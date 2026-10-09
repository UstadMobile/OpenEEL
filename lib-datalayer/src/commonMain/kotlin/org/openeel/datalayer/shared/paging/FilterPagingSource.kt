package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

/**
 * Similar concept to FilterInputStream / FilterOutputStream - will generally just pass things through. Child classes
 * can change.
 *
 * Invalidation is delegated to the src.
 *
 * This is derived from UstadMobile door's DelegatedInvalidationPagingSource and FilterPagingSource
 */
abstract class FilterPagingSource<Key: Any, Value: Any>(
    protected val src: PagingSource<Key, Value>,
    tag: LogPrefixFunction = NO_TAG,
) : DelegatedInvalidationPagingSource<Key, Value>(src, tag){


    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return src.getRefreshKey(state)
    }

    override suspend fun load(params: LoadParams<Key>): LoadResult<Key, Value> {
        registerInvalidationCallbackIfNeeded()
        return src.load(params)
    }


}