package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource

/**
 * Interface that provides a PagingSource. This allows using stateful implementations where required
 * e.g. RepositoryPagingSourceFactory which hosts a RemoteMediator whose lifecycle survives when
 * the local PagingSource is invalidated (e.g. when it's updated by remote data).
 */
fun interface IPagingSourceFactory<Key: Any, Value: Any> {

    operator fun invoke(): PagingSource<Key, Value>

}
