package world.respect.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.repository.ext.copyToValidateOnRemote
import world.respect.datalayer.repository.flow.asRepoFlow
import world.respect.datalayer.school.opds.OpdsFeedDataSource
import world.respect.datalayer.school.opds.OpdsFeedDataSourceLocal
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.lib.dataloadstate.ext.takeIfShouldUpdateLocal
import world.respect.lib.opds.model.OpdsFeed

class OpdsFeedDataSourceRepository(
    val local: OpdsFeedDataSourceLocal,
    val remote: OpdsFeedDataSource,
): OpdsFeedDataSource  {

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return local.getByUrlAsFlow(url, params).asRepoFlow(
            dataLoadParams =  params,
            remoteFlow = {
                remote.getByUrlAsFlow(url, it)
            },
            onRemoteUpdated = {
                local.updateLocal(url, it)
            }
        )
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        val localData = local.getByUrl(url, params)
        val remoteData = remote.getByUrl(
            url = url,
            params = params.copyToValidateOnRemote(localData.metaInfo)
        )

        remoteData.takeIfShouldUpdateLocal(localData)?.also {
            local.updateLocal(url, it)
            return local.getByUrl(url = url, params = params).combineWithRemote(remoteData)
        }

        return localData.combineWithRemote(remoteData)
    }
}