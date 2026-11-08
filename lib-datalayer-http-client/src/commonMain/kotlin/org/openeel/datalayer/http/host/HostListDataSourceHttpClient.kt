package org.openeel.datalayer.http.host

import io.ktor.client.HttpClient
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.host.HostListDataSource
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState

class HostListDataSourceHttpClient(
    private val httpClient: HttpClient,
    private val json: Json,
) : HostListDataSource {

    private suspend fun decodeHosts(response: HttpResponse): List<SchoolDirectory> =
        json.decodeFromString<List<SchoolDirectory>>(response.bodyAsText()).onEach { host ->
            require(!host.name.isNullOrBlank()) {
                "Host ${host.baseUrl} must have a non-blank name"
            }
            requireNotNull(host.icon) {
                "Host ${host.baseUrl} must have an icon URL"
            }
        }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: HostListDataSource.GetListParams,
    ): Flow<DataLoadState<List<SchoolDirectory>>> =
        httpClient.getDataLoadResultAsFlow(
            urlFn = { params.url },
            bodyAdapter = ::decodeHosts,
            requestBuilder = { headers.appendAll(loadParams.requestHeaders) },
        )
}
