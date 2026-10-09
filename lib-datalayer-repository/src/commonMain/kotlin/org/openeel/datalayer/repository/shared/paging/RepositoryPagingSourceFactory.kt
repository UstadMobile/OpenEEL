package world.respect.datalayer.repository.shared.paging

import androidx.paging.PagingSource
import io.github.aakira.napier.Napier
import world.respect.datalayer.shared.DataLayerTags.TAG_DATALAYER
import world.respect.datalayer.shared.paging.DelegatedInvalidationPagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.LogPrefixFunction

/**
 * RepositoryPagingSourceFactory serves two purposes:
 * a) It hoists the remote mediator up such that its lifecycle survives when the local database
 *    paging source is invalidated (e.g. when new data is fetched from the remote server).
 * b) It can hold information on loading status and is accessible to the ViewModel and UI.
 *
 * hoists the remote mediator up such that it survives when the
 * local paging source is invalidated. It can also be used to provide information on loading status.
 */
class RepositoryPagingSourceFactory<T: Any>(
    val onRemoteLoad: suspend (PagingSource.LoadParams<Int>) -> Unit,
    val local: IPagingSourceFactory<Int, T>,
    val tag: LogPrefixFunction = DelegatedInvalidationPagingSource.NO_TAG,
) : IPagingSourceFactory<Int, T> {

    private val remoteMediator = RemoteMediator2(onRemoteLoad)

    private val logPrefix: String by lazy {
        "RPaging/RepositoryPagingSourceFactory(tag = $tag):"
    }

    override fun invoke(): PagingSource<Int, T> {
        Napier.d(tag = TAG_DATALAYER) { "$logPrefix invoke()" }
        return RepositoryOffsetLimitPagingSource2(
            local = local(),
            remoteMediator = remoteMediator,
            tag = tag,
        )
    }
}