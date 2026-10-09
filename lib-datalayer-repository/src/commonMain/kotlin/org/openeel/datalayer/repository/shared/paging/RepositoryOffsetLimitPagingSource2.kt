package world.respect.datalayer.repository.shared.paging

import androidx.paging.PagingSource
import world.respect.datalayer.shared.paging.FilterPagingSource
import io.github.aakira.napier.Napier
import world.respect.datalayer.shared.paging.LogPrefixFunction

/**
 * PagingSource that uses a remote mediator
 *
 * @param remoteMediator RemoteMediator that can trigger remote data loading.
 * @param Local the local data type (may use summary or derivative data from SQL queries etc)
 */
class RepositoryOffsetLimitPagingSource2<Local: Any>(
    internal val local: PagingSource<Int, Local>,
    val remoteMediator: RemoteMediator2,
    tag: LogPrefixFunction = NO_TAG,
) : FilterPagingSource<Int, Local>(
    src = local,
    tag = tag,
){

    private val logPrefix: String by lazy {
        "RPaging/RepositoryOffsetLimitPagingSource2(tag = ${tag()}):"
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Local> {
        Napier.d("$logPrefix load key=${params.key}")
        val localResult = super.load(params)
        remoteMediator.onLocalLoad(params)
        return localResult
    }

}