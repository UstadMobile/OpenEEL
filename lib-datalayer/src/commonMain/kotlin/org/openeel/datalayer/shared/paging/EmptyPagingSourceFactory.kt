package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource

class EmptyPagingSourceFactory<Key: Any, Value: Any>(): IPagingSourceFactory<Key, Value> {

    override fun invoke(): PagingSource<Key, Value> {
        return EmptyPagingSource()
    }
}