package world.respect.datalayer.repository.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingSource.LoadParams
import androidx.paging.PagingSource.LoadResult
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.withContext
import world.respect.datalayer.shared.paging.CacheableHttpPagingSource


suspend fun <T: Any> PagingSource<Int, T>.loadAndUpdateLocal2(
    loadParams: LoadParams<Int>,
    onUpdateLocalFromRemote: suspend (List<T>) -> Unit,
) {
    val remoteLoadResult = load(loadParams)

    withContext(NonCancellable) {
        if(remoteLoadResult is LoadResult.Page) {
            onUpdateLocalFromRemote(remoteLoadResult.data)
        }

        val isNotModifiedResponse = (remoteLoadResult as? LoadResult.Error)?.throwable is
                CacheableHttpPagingSource.NotModifiedNonException

        if(remoteLoadResult is LoadResult.Page || isNotModifiedResponse) {
            @Suppress("UNCHECKED_CAST")
            (this as? CacheableHttpPagingSource<Int, T>)?.onLoadResultStored(remoteLoadResult)
        }
    }
}

