package org.openeel.datalayer.http.school.opds

import io.ktor.client.HttpClient
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
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
        }
    }

}