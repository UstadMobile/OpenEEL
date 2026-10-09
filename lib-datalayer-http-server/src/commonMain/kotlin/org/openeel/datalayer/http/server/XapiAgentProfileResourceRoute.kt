package world.respect.datalayer.http.server

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import kotlinx.serialization.json.Json
import world.respect.datalayer.http.server.ext.receiveXapiDocument
import world.respect.datalayer.http.server.ext.respondXapiDocument
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.resources.XapiAgentProfileResource

fun Route.XapiAgentProfileResourceRoute(
    agentProfileResource: (ApplicationCall) -> XapiAgentProfileResource,
    json: Json,
) {
    get(XapiAgentProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val profileId = call.request.queryParameters["profileId"]
        if (profileId != null) {
            call.respondXapiDocument(
                agentProfileResource(call).get(
                    params = XapiAgentProfileResource.SingleDocumentParams.fromParameters(
                        params = call.request.queryParameters,
                        json = json,
                    ),
                    dataLoadParams = DataLoadParams(
                        requestHeaders = call.request.headers
                    ),
                )
            )
        } else {
            call.respondDataLoadState(
                dataLoadState = agentProfileResource(call).getMultipleDocuments(
                    params = XapiAgentProfileResource.MultiDocParams.fromParameters(
                        params = call.request.queryParameters,
                        json = json,
                    ),
                    dataLoadParams = DataLoadParams(
                        requestHeaders = call.request.headers
                    ),
                ),
            )
        }
    }

    post(XapiAgentProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        try {
            agentProfileResource(call).post(
                params = XapiAgentProfileResource.SingleDocumentParams.fromParameters(
                    params = call.request.queryParameters,
                    json = json,
                ),
                document = call.receiveXapiDocument(),
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }

    put(XapiAgentProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        try {
            agentProfileResource(call).put(
                params = XapiAgentProfileResource.SingleDocumentParams.fromParameters(
                    params = call.request.queryParameters,
                    json = json,
                ),
                document = call.receiveXapiDocument(),
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }

    delete(XapiAgentProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val singleParams = XapiAgentProfileResource.SingleDocumentParams.fromParameters(
            params = call.request.queryParameters,
            json = json,
        )

        try {
            agentProfileResource(call).delete(
                params = singleParams,
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }
}
