package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.xapi.OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.nanohttpd.ext.addXapiCORSHeaders
import world.respect.lib.xapi.nanohttpd.resources.StatementResourceResponder
import world.respect.lib.xapi.nanohttpd.resources.XapiActivityProfileResourceResponder
import world.respect.lib.xapi.nanohttpd.resources.XapiAgentProfileResourceResponder
import world.respect.lib.xapi.nanohttpd.resources.XapiStateResourceResponder
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.lib.xapi.resources.XapiStateResource
import world.respect.lib.xapi.resources.XapiStatementsResource
import java.io.ByteArrayInputStream

class XapiNanoHttpdApp(
    port: Int,
    private val json: Json,
    private val xapiResourceProvider: XapiResourceProvider,
) : NanoHTTPD(port){

    private val statementResourceResponder by lazy {
        StatementResourceResponder(xapiResourceProvider, json)
    }

    private val stateResourceResponder by lazy {
        XapiStateResourceResponder(xapiResourceProvider, json)
    }

    private val activityProfileResourceResponder by lazy {
        XapiActivityProfileResourceResponder(xapiResourceProvider, json)
    }

    private val agentProfileResourceResponder by lazy {
        XapiAgentProfileResourceResponder(xapiResourceProvider, json)
    }

    /**
     * When serving /e/(endpointUrl)/ - the endpoint MUST be double encoded. NanoHTTPD will
     * 'helpfully' decode it, then we won't know what slashes are part of the endpoint and which
     * are part of the api path
     *
     * @param xapiUrl the url of the real upstream xapi server: this will be passed to
     *        XapiNanoHttpdResourceProvider to get the XapiResource
     */
    fun localUrlForEndpoint(
        xapiUrl: Url,
    ): Url {
        //LearningSpace must be double encoded - see note on serveendpoint
        val endpointEncoded = UrlEncoderUtil.encode(
            UrlEncoderUtil.encode(xapiUrl.toString())
        )
        return Url("http://127.0.0.1:$listeningPort${PATH_ENDPOINT_API}$endpointEncoded/")
    }

    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri

        val pathSegments = uri.substring(1).split("/")

        return when {
            uri.startsWith(PATH_ENDPOINT_API) -> {
                serveXapiEndpoint(session, pathSegments)
            }

            else -> {
                newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found: $uri")
            }
        }
    }

    private fun serveXapiEndpoint(
        session: IHTTPSession,
        pathSegments: List<String>,
    ): Response {
        val firstResourceSegmentIndex = if(
            pathSegments[ENDPOINT_SEGMENT_INDEX + 1] == ASSIGNMENT_XAPI_SEGMENT
        ) {
            ENDPOINT_SEGMENT_INDEX + 3
        }else {
            ENDPOINT_SEGMENT_INDEX + 1
        }

        if(session.method == Method.OPTIONS) {
            return newFixedLengthResponse(
                Response.Status.NO_CONTENT,
                "application/json",
                ByteArrayInputStream(byteArrayOf()),
                0,
            ).also {
                it.addXapiCORSHeaders(session)
                logResponse(session, it)
            }
        }

        val resourceSegment1 = pathSegments[firstResourceSegmentIndex]
        val resourceSegment2 = pathSegments.getOrNull(firstResourceSegmentIndex + 1)

        return runBlocking {
            try {
                Napier.i(
                    tag = LOGTAG,
                    message = "${session.method} ${session.uri}"
                )

                when {
                    resourceSegment1 == XapiStatementsResource.ENDPOINT_NAME -> {
                        statementResourceResponder.serveXapiEndpoint(session, pathSegments)
                    }

                    (resourceSegment1 == "activities" && resourceSegment2 == XapiStateResource.ENDPOINT_NAME) ||
                        resourceSegment1 == XapiStateResource.ENDPOINT_NAME -> {
                        stateResourceResponder.serveXapiEndpoint(session, pathSegments)
                    }

                    resourceSegment1 == "activities" && resourceSegment2 == XapiActivityProfileResource.ENDPOINT_NAME -> {
                        activityProfileResourceResponder.serveXapiEndpoint(session, pathSegments)
                    }

                    resourceSegment1 == "agents" && resourceSegment2 == XapiAgentProfileResource.ENDPOINT_NAME -> {
                        agentProfileResourceResponder.serveXapiEndpoint(session, pathSegments)
                    }

                    else -> {
                        newFixedLengthResponse(
                            Response.Status.NOT_FOUND,
                            "text/plain",
                            "not found: ${session.uri}"
                        ).also {
                            it.addXapiCORSHeaders(session)
                        }
                    }
                }
            }catch(e: Throwable) {
                val responseStatus = Response.Status.lookup(
                    (e as? XapiException)?.httpStatusCode ?: 500
                )

                Napier.e(
                    tag = LOGTAG,
                    message = "Error serving: ${session.method} ${session.uri} (status=$responseStatus)",
                    throwable = e,
                )

                newFixedLengthResponse(
                    responseStatus,
                    "text/plain",
                    e.message ?: "No error message",
                ).also {
                    it.addXapiCORSHeaders(session)
                }
            }
        }
    }


    companion object {

        const val PATH_ENDPOINT_API = "/e/"

        /**
         * Requests that put the endpoint into a path will be in the form of /e/<endpoint-url>/...
         * so to get the endpoint itself (eg https://school.example.org/) the segment index is 1
         */
        const val ENDPOINT_SEGMENT_INDEX = 1

        const val LOGTAG = "XapiNanoHttpd"

    }
}