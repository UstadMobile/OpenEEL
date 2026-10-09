package world.respect.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
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
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.libutil.ext.appendEndpointSegments

class XapiActivityProfileResourceHttpClient(
    private val xapiUrl: suspend () -> Url,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
): XapiActivityProfileResource {

    private suspend fun XapiActivityProfileResource.MultiDocParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments("activities/${XapiActivityProfileResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters())
        }.build()
    }

    private suspend fun XapiActivityProfileResource.SingleDocumentParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments("activities/${XapiActivityProfileResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters())
        }.build()
    }

    override suspend fun getMultipleDocuments(
        params: XapiActivityProfileResource.MultiDocParams,
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
        params: XapiActivityProfileResource.SingleDocumentParams,
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
        params: XapiActivityProfileResource.SingleDocumentParams,
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
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.post(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            setXapiDocumentBody(document)
        }.throwXapiExceptionIfNotSuccessful()
    }

    override suspend fun put(
        params: XapiActivityProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.put(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            setXapiDocumentBody(document)
        }.throwXapiExceptionIfNotSuccessful()
    }

    override suspend fun delete(params: XapiActivityProfileResource.SingleDocumentParams) {
        httpClient.delete(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
        }.throwXapiExceptionIfNotSuccessful()
    }
}