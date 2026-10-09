package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.bodyAsXapiDocument
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.school.xapi.ext.setXapiDocumentBody
import world.respect.datalayer.http.school.xapi.ext.throwXapiExceptionIfNotSuccessful
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.libutil.ext.appendEndpointSegments

class XapiAgentProfileResourceHttpClient(
    private val xapiUrl: suspend () -> Url,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): XapiAgentProfileResource {

    private suspend fun XapiAgentProfileResource.MultiDocParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments("agents/${XapiAgentProfileResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters(json))
        }.build()
    }

    private suspend fun XapiAgentProfileResource.SingleDocumentParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments("agents/${XapiAgentProfileResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters(json))
        }.build()
    }

    override suspend fun getMultipleDocuments(
        params: XapiAgentProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<List<String>> {
        return httpClient.getAsDataLoadState<List<String>>(
            url = params.urlWithParams(),
        ) {
            useTokenProvider(tokenProvider)
            headers.appendAll(dataLoadParams.requestHeaders)
        }
    }

    override suspend fun get(
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiDocument> {
        return httpClient.getAsDataLoadState(
            url = params.urlWithParams(),
            bodyAdapter = {
                it.bodyAsXapiDocument()
            }
        ) {
            useTokenProvider(tokenProvider)
            headers.appendAll(dataLoadParams.requestHeaders)
        }
    }

    override fun getAsFlow(
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiDocument>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = { params.urlWithParams() },
            bodyAdapter = { it.bodyAsXapiDocument() },
        ) {
            useTokenProvider(tokenProvider)
            headers.appendAll(dataLoadParams.requestHeaders)
        }
    }

    override suspend fun post(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.post(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            setXapiDocumentBody(document)
        }.throwXapiExceptionIfNotSuccessful()
    }

    override suspend fun put(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.put(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            setXapiDocumentBody(document)
        }.throwXapiExceptionIfNotSuccessful()
    }

    override suspend fun delete(params: XapiAgentProfileResource.SingleDocumentParams) {
        httpClient.delete(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
        }.throwXapiExceptionIfNotSuccessful()
    }
}
