package world.respect.lib.dataloadstate.ext

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.NoDataLoadedState
import io.ktor.http.HttpHeaders
import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate

fun <T: Any> DataLoadState<T>.combineWithRemote(
    remote: DataLoadState<*>
): DataLoadState<T> {
    return this.copyLoadState(
        metaInfo = metaInfo.copy(
            url = remote.metaInfo.url
        ),
        localState = this,
        remoteState = remote
    )
}

fun <T: Any> DataLoadState<T>.combineWithRemoteIfNotNull(
    remote: DataLoadState<*>?
): DataLoadState<T> {
    return if(remote != null) {
        this.combineWithRemote(remote)
    }else {
        this
    }
}

fun <T: Any> Flow<DataLoadState<T>>.combineWithRemote(
    remoteFlow: Flow<DataLoadState<*>>,
): Flow<DataLoadState<T>> =combine(remoteFlow) { local, remote ->
    local.combineWithRemote(remote)
}

fun <T: Any> DataLoadState<T>.copyLoadState(
    metaInfo: DataLoadMetaInfo = this.metaInfo,
    localState: DataLoadState<T>? = this.localState,
    remoteState: DataLoadState<*>? = this.remoteState,
) : DataLoadState<T> {
    return when(this) {
        is DataReadyState -> copy(
            metaInfo = metaInfo,
            localState = localState,
            remoteState = remoteState,
        )
        is DataLoadingState -> copy(
            metaInfo = metaInfo,
            localState = localState,
            remoteState = remoteState,
        )
        is DataErrorResult -> copy(
            metaInfo = metaInfo,
            localState = localState,
            remoteState = remoteState,
        )
        is NoDataLoadedState -> copy(
            metaInfo = metaInfo,
            localState = localState,
            remoteState = remoteState,
        )
    }
}

fun <T: Any> DataLoadState<T>.dataOrNull(): T? {
    return (this as? DataReadyState)?.data ?: (this as? DataLoadingState)?.partialData
}


/**
 *  A DataReadyState may be provided when local data is available, however update checks are still
 *  going on in the background. In the case of an edit screen, we may want to show local data to
 *  a user but not allow editing until the remote data is check is done (if possible).
 *
 *  @return true if the DataLoadState is DataReadyState and there are no pending remote updates.
 */
fun DataLoadState<*>.isReadyAndSettled(): Boolean {
    return this is DataReadyState && this.remoteState !is DataLoadingState
}

fun DataLoadState<*>.isLoadedOrNotModified(): Boolean {
    return this is DataReadyState ||
            (this is NoDataLoadedState && reason == NoDataLoadedState.Reason.NOT_MODIFIED)
}

/**
 * Transform a DataReadyState with a transformation that can act on the data. If not DataReadyState,
 * then return the original DataLoadState.
 */
fun <T: Any, R: Any> DataLoadState<T>.mapDataReadyState(
    transform: (DataReadyState<T>) -> DataLoadState<R>
): DataLoadState<R> {
    return when(this) {
        is DataReadyState -> {
            transform(this)
        }

        is DataLoadingState -> {
            DataLoadingState(
                metaInfo = metaInfo,
                localState = localState?.mapDataReadyState(transform),
                remoteState = remoteState,
            )
        }
        is DataErrorResult -> {
            DataErrorResult(
                error = error,
                metaInfo = metaInfo,
                localState = localState?.mapDataReadyState(transform),
                remoteState = remoteState,
            )
        }
        is NoDataLoadedState -> {
            NoDataLoadedState(
                reason = reason,
                metaInfo = metaInfo,
                localState = localState?.mapDataReadyState(transform),
                remoteState = remoteState,
            )
        }
    }
}

/**
 * Transform a DataReadyState with a suspending transformation that can act on the data. If not DataReadyState,
 * then return the original DataLoadState.
 */
suspend fun <T: Any, R: Any> DataLoadState<T>.mapDataReadyStateAsync(
    transform: suspend (DataReadyState<T>) -> DataLoadState<R>
): DataLoadState<R> {
    return when(this) {
        is DataReadyState -> {
            transform(this)
        }

        is DataLoadingState -> {
            DataLoadingState(
                metaInfo = metaInfo,
                localState = localState?.mapDataReadyStateAsync(transform),
                remoteState = remoteState,
            )
        }
        is DataErrorResult -> {
            DataErrorResult(
                error = error,
                metaInfo = metaInfo,
                localState = localState?.mapDataReadyStateAsync(transform),
                remoteState = remoteState,
            )
        }
        is NoDataLoadedState -> {
            NoDataLoadedState(
                reason = reason,
                metaInfo = metaInfo,
                localState = localState?.mapDataReadyStateAsync(transform),
                remoteState = remoteState,
            )
        }
    }
}

suspend fun <T: Any, R: Any> DataLoadState<T>.mapAsync(
    transform: suspend (T) -> R
): DataLoadState<R> {
    return mapDataReadyStateAsync {
        DataReadyState(
            data = transform(it.data),
            metaInfo = metaInfo,
            localState = localState?.mapAsync(transform),
            remoteState = remoteState,
        )
    }
}


fun <T: Any, R: Any> DataLoadState<T>.map(
    transform: (T) -> R
): DataLoadState<R> {
    return mapDataReadyState {
        DataReadyState(
            data = transform(it.data),
            metaInfo = metaInfo,
            localState = localState?.map(transform),
            remoteState = remoteState,
        )
    }
}

/**
 * Convert the given DataLoadState into a NoDataLoadedState if the list is empty.
 */
fun <T: Any> DataLoadState<List<T>>.notLoadedIfEmpty(): DataLoadState<List<T>> {
    return mapDataReadyState {
        if(it.data.isNotEmpty()) {
            DataReadyState(
                data = it.data,
                metaInfo = metaInfo,
                localState = localState?.notLoadedIfEmpty(),
                remoteState = remoteState,
            )
        }else {
            NoDataLoadedState(
                reason = NoDataLoadedState.Reason.NOT_FOUND,
                metaInfo = metaInfo,
                localState = localState?.notLoadedIfEmpty(),
                remoteState = remoteState,
            )
        }
    }
}

/**
 * Sometimes a list http endpoint is used, even though there is only going to be one result at most
 * e.g. Person when searched for a specific guid or username.
 *
 * @receiver a DataLoadState
 * @return If the DataLoadState is DataReadyState, and the list has at least one item, then a
 *         DataReadyState with the first item.
 *         If the DataLoadState is DataReadyState and the list is empty, then a NoDataLoadedState
 *         Otherwise, the original DataLoadState
 *
 */
fun <T: Any> DataLoadState<List<T>>.firstOrNotLoaded(): DataLoadState<T> {
    return mapDataReadyState {
        if(it.data.isNotEmpty()) {
            DataReadyState(
                data = it.data.first(),
                metaInfo = metaInfo,
                localState = localState?.firstOrNotLoaded(),
                remoteState = remoteState,
            )
        }else {
            NoDataLoadedState(
                reason = NoDataLoadedState.Reason.NOT_FOUND,
                metaInfo = metaInfo,
                localState = localState?.firstOrNotLoaded(),
                remoteState = remoteState,
            )
        }
    }
}

/**
 * Return the Last-Modified header as it should be put on the HttpResponse, if any. An explicitly
 * set header take precedence.
 */
fun DataLoadState<*>.lastModifiedForHttpResponseHeader(): String? {
    return metaInfo.headers[HttpHeaders.LastModified]
        ?: metaInfo.lastStored.takeIf { it > 0 }?.let { GMTDate(it).toHttpDate() }
        ?: metaInfo.lastModified.takeIf { it > 0 }?.let { GMTDate(it).toHttpDate() }
}

fun DataLoadState<*>.etagForHttpResponseHeader(): String? {
    return metaInfo.headers[HttpHeaders.ETag] ?: metaInfo.etag
}

fun DataLoadState<*>.toPrettyString(): String {
    return when(this) {
        is DataReadyState -> {
           if(data is List<*>) {
               "DataReadyState ${data.size} items"
           }else {
               "DataReadyState"
           }
        }
        is DataLoadingState -> "DataLoadingState"

        is DataErrorResult -> "DataErrorResult"
        is NoDataLoadedState -> "NoDataLoadedState($reason)"
    }
}

/**
 * Determine if the receiver remote [DataLoadState] should be used to update the local state.
 *
 * @receiver [DataLoadState] from the remote source
 * @param localState [DataLoadState] from the local source
 * @return the remote [DataLoadState] as [DataReadyState] that should be applied to the local
 *         source, or null if it should not be applied. This will be true when:
 *         1. The remote (receiver) [DataLoadState] is [DataReadyState]
 *         2. The localState is either a) NOT a [DataReadyState] eg because the data has not
 *            been stored locally yet OR b) the remote (receiver) has a more recent Last-Modified
 *            than the localState.
 */
fun <T: Any> DataLoadState<T>.takeIfShouldUpdateLocal(
    localState: DataLoadState<*>,
): DataReadyState<T>? {
    return when {
        this !is DataReadyState -> null
        localState is DataReadyState -> {
            this.takeIf {
                this.metaInfo.headers.responseETagAndLastModified().isNewer(
                    localState.metaInfo.headers.responseETagAndLastModified()
                ) == true
            }
        }
        else -> this
    }
}
