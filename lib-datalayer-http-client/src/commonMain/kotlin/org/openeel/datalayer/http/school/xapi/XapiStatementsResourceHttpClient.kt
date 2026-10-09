package world.respect.datalayer.http.school.xapi

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
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.toDataLoadState
import world.respect.datalayer.ext.useTokenProvider
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.xapi.composites.AssignmentAndProgress
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.lib.xapi.resources.XapiStatementsResource.GetStatementParams
import world.respect.libutil.ext.appendEndpointSegments
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