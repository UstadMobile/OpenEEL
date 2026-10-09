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
import world.respect.datalayer.http.server.ext.receiveXapiDocument
import world.respect.datalayer.http.server.ext.respondXapiDocument
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.resources.XapiActivityProfileResource

fun Route.XapiActivityProfileResourceRoute(
    activityProfileResource: (ApplicationCall) -> XapiActivityProfileResource,
) {
    get(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val profileId = call.request.queryParameters["profileId"]
        if (profileId != null) {
            call.respondXapiDocument(
                activityProfileResource(call).get(
                    params = XapiActivityProfileResource.SingleDocumentParams.fromParameters(
                        params = call.request.queryParameters
                    ),
                    dataLoadParams = DataLoadParams(
                        requestHeaders = call.request.headers
                    ),
                )
            )
        } else {
            call.respondDataLoadState(
                dataLoadState = activityProfileResource(call).getMultipleDocuments(
                    params = XapiActivityProfileResource.MultiDocParams.fromParameters(
                        params = call.request.queryParameters
                    ),
                    dataLoadParams = DataLoadParams(
                        requestHeaders = call.request.headers
                    ),
                ),
            )
        }
    }

    post(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        try {
            activityProfileResource(call).post(
                params = XapiActivityProfileResource.SingleDocumentParams.fromParameters(
                    params = call.request.queryParameters
                ),
                document = call.receiveXapiDocument(),
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }

    put(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        try {
            activityProfileResource(call).put(
                params = XapiActivityProfileResource.SingleDocumentParams.fromParameters(
                    params = call.request.queryParameters
                ),
                document = call.receiveXapiDocument(),
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }

    delete(XapiActivityProfileResource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)

        val singleParams = XapiActivityProfileResource.SingleDocumentParams.fromParameters(
            params = call.request.queryParameters
        )

        try {
            activityProfileResource(call).delete(
                params = singleParams,
            )
            call.respond(HttpStatusCode.NoContent)
        } catch (e: XapiException) {
            call.respond(HttpStatusCode.fromValue(e.httpStatusCode), e.message ?: "")
        }
    }
}
