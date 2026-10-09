package org.openeel.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.datalayer.ext.bodyAsXapiDocument
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.ext.useTokenProvider
import org.openeel.datalayer.http.school.xapi.ext.setXapiDocumentBody
import org.openeel.datalayer.http.school.xapi.ext.throwXapiExceptionIfNotSuccessful
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.libutil.ext.appendEndpointSegments

class XapiStateResourceHttpClient(
    private val xapiUrl: suspend () -> Url,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): XapiStateResource {

    private suspend fun XapiStateResource.MultiDocParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments("activities/${XapiStateResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters(json))
        }.build()
    }

    private suspend fun XapiStateResource.SingleDocumentParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments("activities/${XapiStateResource.ENDPOINT_NAME}")).also {
            it.parameters.appendAll(this.toParameters(json))
        }.build()
    }

    override suspend fun getMultipleDocuments(
        params: XapiStateResource.MultiDocParams,
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
        params: XapiStateResource.SingleDocumentParams,
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
        params: XapiStateResource.SingleDocumentParams,
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
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.post(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            setXapiDocumentBody(document)
        }.throwXapiExceptionIfNotSuccessful()
    }

    override suspend fun put(
        params: XapiStateResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        httpClient.put(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
            setXapiDocumentBody(document)
        }.throwXapiExceptionIfNotSuccessful()
    }

    override suspend fun delete(params: XapiStateResource.SingleDocumentParams) {
        httpClient.delete(params.urlWithParams()) {
            useTokenProvider(tokenProvider)
        }.throwXapiExceptionIfNotSuccessful()
    }
}
