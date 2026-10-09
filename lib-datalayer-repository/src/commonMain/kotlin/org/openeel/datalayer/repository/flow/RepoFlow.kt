package world.respect.datalayer.repository.flow

import io.ktor.http.Headers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import world.respect.datalayer.repository.ext.copyToValidateOnRemote
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.lib.dataloadstate.ext.responseETagAndLastModified
import world.respect.lib.dataloadstate.ext.isStillValid
import world.respect.lib.dataloadstate.ext.takeIfShouldUpdateLocal

const val LOADED_DEQUE_SIZE = 4

/**
 * Create an offline-first repository flow that will:
 *
 * a) Immediately emit the local data, if available.
 * b) Asynchronously collects the remote data. The ETag and Last-Modified headers (if provided) from
 *    the local data are used to set the request headers on the DataLoadParams passed to the remote.
 * c) Invokes onRemoteUpdated when new remote data is ready. This should generally then be used to
 *    update the local data source (e.g. using the updateLocal function).
 *
 * @receiver A Flow from a local datasource
 * @param dataLoadParams the base DataLoadParams to use when requesting remote data.
 * @param remoteFlow a function that returns a flow of the remote data for the given [DataLoadParams].
 *        The [DataLoadParams] will include If-Modified-Since and If-None-Match headers from the
 *        local data where available. Recent duplicate emissions from the receiver local flow (as per
 *        ETag/LastModified headers) will be filtered so that remoteFlow will only be invoked once
 *        per distinct local data emission.
 * @return A combined flow of the local and remote data.
 */
fun <T: Any, R: Any> Flow<DataLoadState<T>>.asRepoFlow(
    dataLoadParams: DataLoadParams,
    remoteFlow: (DataLoadParams) -> Flow<DataLoadState<R>>,
    onRemoteUpdated: suspend (DataReadyState<R>) -> Unit,
): Flow<DataLoadState<T>> {
    return channelFlow {
        val remoteFlowState = MutableStateFlow<DataLoadState<R>>(DataLoadingState())

        val sharedLocal = this@asRepoFlow.shareIn(
            scope = this, started = SharingStarted.Lazily
        ).distinctUntilChanged { old, new ->
            old.metaInfo.headers.responseETagAndLastModified().isStillValid(
                other = new.metaInfo.headers.responseETagAndLastModified()
            )
        }

        launch {
            sharedLocal.combine(remoteFlowState) { localState, remoteState ->
                localState.combineWithRemote(remoteState)
            }.collect {
                send(it)
            }
        }

        launch {
            val remoteLoadedDeque = ArrayDeque<Headers>(LOADED_DEQUE_SIZE)
            sharedLocal.filter { newLocalState ->
                /* When new remote data is loaded, this normally leads to updating the local
                 * datasource, which then leads to the local flow emitting the new data. We want
                 * to avoid triggering _another_ request to the remote datasource for the same data
                 * we just received from it.
                 *
                 * This is done by keeping the last few headers.
                 */
                val newLocalStateEtagAndLastModified = newLocalState.metaInfo.headers
                    .responseETagAndLastModified()

                !remoteLoadedDeque.any { prevRemoteHeaders ->
                    prevRemoteHeaders.responseETagAndLastModified().isStillValid(
                        other = newLocalStateEtagAndLastModified
                    )
                }
            }.collectLatest { localState ->
                remoteFlow(
                    dataLoadParams.copyToValidateOnRemote(localState.metaInfo)
                ).collect { remoteState ->
                    remoteFlowState.value = remoteState

                    /*
                     * Filter out remote data that is older than the data we have locally. As per
                     * the HTTP spec when both If-None-Match and If-Not-Modified-Since are provided
                     * then If-Not-Modified-Since is ignored.
                     *
                     * Left unchecked, this would lead to a situation where the older remote data
                     * would overwrite newer local data.
                     */
                    remoteState.takeIfShouldUpdateLocal(localState)?.also {
                        onRemoteUpdated(it)
                        if(remoteLoadedDeque.size > LOADED_DEQUE_SIZE) {
                            remoteLoadedDeque.removeLast()
                        }
                        remoteLoadedDeque.addFirst(remoteState.metaInfo.headers)
                    }
                }
            }
        }
    }
}