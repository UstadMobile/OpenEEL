package world.respect.lib.dataloadstate

import kotlinx.serialization.Serializable

/**
 * Wrapper interface for the state of data being loaded. Allows a data layer flow to contain
 * both the data and the loading state.
 *
 * @property metaInfo metadata about the data being loaded e.g. URL, last modified info, etc.
 * @property localState the state of the data as loaded from the local data source, if applicable.
 *           This is set by the Repository when there is a distinct local and network result.
 * @property remoteState the state of the data as loaded from the network data source, if applicable.
 *           This is set by the Repository when there is a distinct local and network result.
 *           It is not typed because the remote state is often a different type to what is being
 *           displayed e.g. a list screen will show only basic details, but when the list is
 *           loaded over the network, the full entity will be fetched.
 */

@Serializable
sealed interface DataLoadState<T: Any> {
    val metaInfo: DataLoadMetaInfo
    val localState: DataLoadState<T>?
    val remoteState: DataLoadState<*>?


    companion object {

        fun <T: Any> readyOrNotFoundIfNull(
            data: T?,
            metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
            localState: DataLoadState<T>? = null,
            remoteState: DataLoadState<*>? = null,
        ): DataLoadState<T> {
            return data?.let {
                DataReadyState(
                    data = it,
                    metaInfo = metaInfo,
                    localState = localState,
                    remoteState = remoteState,
                )
            } ?: NoDataLoadedState(
                reason = NoDataLoadedState.Reason.NOT_FOUND,
                metaInfo = metaInfo,
                localState = localState,
                remoteState = remoteState,
            )
        }

    }
}

/**
 * Data loading is in progress
 *
 * @param partialData this can be used to provide partial data results. This is only rarely needed,
 *        intended to be used for APIs where the HTTP implementation connects to multiple servers
 *        concurrently where we want to be able to show the user the results received so far,
 *        indicate that loading is still in progress, and avoid waiting for all servers to respond
 *        in the meantime (which could take a while if one or more servers are down or slow).
 *
 */
@Serializable
data class DataLoadingState<T: Any>(
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
    override val localState: DataLoadState<T>? = null,
    override val remoteState: DataLoadState<*>? = null,
    val partialData: T? = null,
): DataLoadState<T>

/**
 * Data has not been loaded and this is not an error - e.g. when a remote datasource returns 304
 * not modified, no content, or when the local datasource is empty.
 */
@Serializable
data class NoDataLoadedState<T: Any>(
    val reason: Reason,
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
    override val localState: DataLoadState<T>? = null,
    override val remoteState: DataLoadState<*>? = null,
): DataLoadState<T> {

    enum class Reason {
        NOT_MODIFIED, NOT_FOUND;

        companion object {

            fun forStatusCode(code: Int): Reason {
                return when(code) {
                    302 -> NOT_MODIFIED
                    404 -> NOT_FOUND
                    else -> throw IllegalStateException("Unknown status code $code")
                }
            }

        }
    }

    companion object {

        @Suppress("unused") //reserved for future use
        fun <T: Any> notModified(
            metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
        ): NoDataLoadedState<T> = NoDataLoadedState(
            reason = Reason.NOT_MODIFIED,
            metaInfo = metaInfo,
        )

        @Suppress("unused") //reserved for future use
        fun <T: Any> notFound(
            metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
        ): NoDataLoadedState<T> = NoDataLoadedState(
            reason = Reason.NOT_FOUND,
            metaInfo = metaInfo,
        )

    }

}

/**
 * Data is loaded and ready. If loaded from a repository, it may be that the local data is ready
 * for display and the remote data is still being loaded.
 */
@Serializable
data class DataReadyState<T: Any>(
    val data: T,
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
    override val localState: DataLoadState<T>? = null,
    override val remoteState: DataLoadState<*>? = null,
): DataLoadState<T>

/**
 * There has been an error loading data.
 */
data class DataErrorResult<T: Any>(
    val error: Throwable,
    override val metaInfo: DataLoadMetaInfo = DataLoadMetaInfo(),
    override val localState: DataLoadState<T>? = null,
    override val remoteState: DataLoadState<*>? = null,
): DataLoadState<T>

