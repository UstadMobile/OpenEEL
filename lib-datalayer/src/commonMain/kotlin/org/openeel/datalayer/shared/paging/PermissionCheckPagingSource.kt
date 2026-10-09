package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CompletableDeferred
import world.respect.datalayer.exceptions.ForbiddenException

/**
 * A simple paging source wrapper that will run a permission check function before loading data.
 */
class PermissionCheckPagingSource<Key: Any, Value: Any>(
    private val src: PagingSource<Key, Value>,
    tag: LogPrefixFunction = NO_TAG,
    private val onCheckPermission: suspend () -> Boolean,
): DelegatedInvalidationPagingSource<Key, Value>(
    invalidationDelegate = src,
    tag = tag,
) {
    private val completable = CompletableDeferred<Boolean>()

    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return src.getRefreshKey(state)
    }

    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> {
        registerInvalidationCallbackIfNeeded()

        val hasPermission = if(!completable.isCompleted) {
            val permissionResult = onCheckPermission()
            completable.complete(permissionResult)
            permissionResult
        }else {
            completable.await()
        }

        return if(hasPermission) {
            src.load(params)
        }else {
            LoadResult.Error(ForbiddenException("Permission denied"))
        }
    }
}