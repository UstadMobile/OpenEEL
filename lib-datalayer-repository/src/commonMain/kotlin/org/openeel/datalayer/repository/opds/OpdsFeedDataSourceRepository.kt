package org.openeel.datalayer.repository.opds

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.repository.ext.copyToValidateOnRemote
import org.openeel.datalayer.repository.flow.asRepoFlow
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.datalayer.school.opds.OpdsFeedDataSourceLocal
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.lib.dataloadstate.ext.takeIfShouldUpdateLocal
import org.openeel.lib.opds.model.OpdsFeed

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