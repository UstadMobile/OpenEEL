package org.openeel.datalayer.http.server

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ktorserver.respondDataLoadState
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.lib.xapi.model.XapiSingleItemToListSerializer
import org.openeel.lib.xapi.model.XapiStatement

fun Route.XapiStatementsResourceRoute(
    statementResource: (ApplicationCall) -> XapiStatementsResource,
    json: Json,
    processStatementsUseCase: ((ApplicationCall) -> ProcessXapiStatementsUseCase)? = null
) {

    get(XapiStatementsResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val statementResponse = statementResource(call).get(
            listParams = XapiStatementsResource.GetStatementParams.fromParams(
                params = call.request.queryParameters,
                json = json,
            ),
            dataLoadParams = DataLoadParams()
        )

        call.respondDataLoadState(statementResponse)
    }

    post(XapiStatementsResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val statementsJson: JsonElement = call.receive()
        val statements: List<XapiStatement> = json.decodeFromJsonElement(
            XapiSingleItemToListSerializer, statementsJson
        )

        val storeResult = statementResource(call).post(statements)

        processStatementsUseCase?.invoke(call)?.let { useCase ->
            call.application.launch {
                try {
                    useCase(statements)
                } catch (e: Exception) {
                    println("XapiStatementsResourceRoute: Error processing statements: ${e.message}")
                }
            }
        }

        call.respondDataLoadState(storeResult)
    }
}
