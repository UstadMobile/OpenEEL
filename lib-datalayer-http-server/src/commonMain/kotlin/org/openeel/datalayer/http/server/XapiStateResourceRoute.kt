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
import world.respect.lib.xapi.resources.XapiStateResource

fun Route.XapiStateResourceRoute(
    stateResource: (ApplicationCall) -> XapiStateResource,
    json: Json,
) {
    get(XapiStateResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val stateId = call.request.queryParameters["stateId"]
        if (stateId != null) {
            call.respondXapiDocument(
                stateResource(call).get(
                    params = XapiStateResource.SingleDocumentParams.fromParameters(
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
                dataLoadState = stateResource(call).getMultipleDocuments(
                    params = XapiStateResource.MultiDocParams.fromParameters(
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

    post(XapiStateResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        try {
            stateResource(call).post(
                params = XapiStateResource.SingleDocumentParams.fromParameters(
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

    put(XapiStateResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        try {
            stateResource(call).put(
                params = XapiStateResource.SingleDocumentParams.fromParameters(
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

    delete(XapiStateResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val singleParams = XapiStateResource.SingleDocumentParams.fromParameters(
            params = call.request.queryParameters,
            json = json,
        )

        try {
            stateResource(call).delete(
                params = singleParams,
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }
}
