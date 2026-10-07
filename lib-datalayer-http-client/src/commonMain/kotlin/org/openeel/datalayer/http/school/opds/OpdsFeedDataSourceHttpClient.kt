package org.openeel.datalayer.http.school.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.datalayer.school.opds.ext.withAbsoluteSelfUrl
import org.openeel.lib.opds.model.OpdsFeed

class OpdsFeedDataSourceHttpClient(
    private val httpClient: HttpClient,
): OpdsFeedDataSource {

    override fun getByUrlAsFlow(
        url: Url,
        params: DataLoadParams
    ): Flow<DataLoadState<OpdsFeed>> {
        return httpClient.getDataLoadResultAsFlow<OpdsFeed>(
            url = url,
            dataLoadParams = params,
        ) {
            headers.appendAll(params.requestHeaders)
        }.map { loadResult ->
            /*
             * When the OpdsFeed is persisted to the database, the self url is used to determine the
             * url. The Url is hashed to make the primary key.
             */
            loadResult.map { it.withAbsoluteSelfUrl(url) }
        }
    }

    override suspend fun getByUrl(
        url: Url,
        params: DataLoadParams
    ): DataLoadState<OpdsFeed> {
        return httpClient.getAsDataLoadState<OpdsFeed>(
            url = url,
        ) {
            headers.appendAll(params.requestHeaders)
        }.map {
            it.withAbsoluteSelfUrl(url)
        }
    }

}