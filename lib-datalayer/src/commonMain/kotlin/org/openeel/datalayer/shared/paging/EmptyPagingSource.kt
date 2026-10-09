package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState

class EmptyPagingSource<Key: Any, Value: Any>: PagingSource<Key, Value>() {

    override fun getRefreshKey(state: PagingState<Key, Value>): Key? {
        return null
    }

    override suspend fun load(
        params: LoadParams<Key>
    ): LoadResult<Key, Value> {
        return LoadResult.Page(emptyList(), null, null)
    }
}

