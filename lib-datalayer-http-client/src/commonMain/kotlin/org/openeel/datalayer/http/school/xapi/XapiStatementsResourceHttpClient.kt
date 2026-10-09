package org.openeel.datalayer.http.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.ext.toDataLoadState
import org.openeel.datalayer.ext.useTokenProvider
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.xapi.composites.AssignmentAndProgress
import org.openeel.lib.xapi.model.AssignmentSummary
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import org.openeel.libutil.ext.appendEndpointSegments
import kotlin.uuid.Uuid

class XapiStatementsResourceHttpClient(
    private val httpClient: HttpClient,
    private val xapiUrl: suspend () -> Url,
    private val tokenProvider: AuthTokenProvider,
    private val json: Json,
): XapiStatementsResource {

    private suspend fun GetStatementParams.urlWithParams(): Url {
        return URLBuilder(xapiUrl().appendEndpointSegments(XapiStatementsResource.ENDPOINT_NAME)).also {
            it.parameters.appendAll(this.toParameters(json))
        }.build()
    }

    override suspend fun post(list: List<XapiStatement>): DataLoadState<List<Uuid>> {
        return httpClient.post(
            url = xapiUrl().appendEndpointSegments(XapiStatementsResource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)

            contentType(ContentType.Application.Json)
            setBody(list)
        }.toDataLoadState(typeInfo<List<Uuid>>())
    }

    override suspend fun get(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiStatementResult> {

        return httpClient.getAsDataLoadState<XapiStatementResult>(
            url = listParams.urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
        }
    }

    override fun getAsFlow(
        listParams: GetStatementParams,
        dataLoadParams: DataLoadParams
    ): Flow<DataLoadState<XapiStatementResult>> {
        return httpClient.getDataLoadResultAsFlow<XapiStatementResult>(
            urlFn = {
                listParams.urlWithParams()
            },
            dataLoadParams = dataLoadParams,
        ) {
            useTokenProvider(tokenProvider)
        }
    }

    override fun getAssignmentProgress(
        activityId: String,
        filterByAssigneeAgent: XapiAgent?
    ): Flow<DataLoadState<AssignmentAndProgress>> {
        throw IllegalStateException("GetAssignmentResults over HTTP is not supported")
    }

    override fun getAssignmentListAsFlow(dataLoadParams: DataLoadParams, studentAgent: XapiAgent?): Flow<DataLoadState<List<AssignmentSummary>>> {
        throw IllegalStateException("GetAssignmentResults over HTTP is not supported")
    }
}