package org.openeel.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import io.ktor.util.StringValuesImpl
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.ext.asAssignmentRecipeStmtIfIdNotNull
import org.openeel.lib.xapi.ext.put
import org.openeel.lib.xapi.model.XapiSingleItemToListSerializer
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.nanohttpd.ext.addXapiCORSHeaders
import org.openeel.lib.xapi.nanohttpd.ext.bodyAsBytes
import org.openeel.lib.xapi.resources.XapiStatementsResource
import java.io.ByteArrayInputStream
import kotlin.uuid.Uuid

class XapiNanoHttpdApp(
    port: Int,
    private val json: Json,
    private val xapiResourceProvider: XapiResourceProvider,
) : NanoHTTPD(port){

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

    fun <T: Any> DataLoadState<T>.toFixedLengthResponse(
        serializer: SerializationStrategy<T>
    ): Response {
        val jsonText = this.dataOrNull()?.let {
            json.encodeToString(serializer, it)
        } ?: return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "not found")

        return newFixedLengthResponse(
            Response.Status.OK, "application/json", jsonText
        )
    }

    private fun logResponse(
        session: IHTTPSession,
        response: Response
    ) {
        Napier.i(
            tag = LOGTAG,
            message = "HTTP ${response.status.requestStatus}: ${session.method} ${session.uri}"
        )
    }

    private fun serveXapiEndpoint(
        session: IHTTPSession,
        pathSegments: List<String>,
    ): Response {
        val endpointUrl = Url(UrlEncoderUtil.decode(
            pathSegments[ENDPOINT_SEGMENT_INDEX])
        )
        val authentication = session.headers["authorization"]

        val nextSegment = pathSegments[ENDPOINT_SEGMENT_INDEX + 1]

        val assignmentXform = nextSegment == ASSIGNMENT_XAPI_SEGMENT
        val assignmentActivityId = if(assignmentXform) {
            UrlEncoderUtil.decode(pathSegments[ENDPOINT_SEGMENT_INDEX + 2])
        }else {
            null
        }

        return runBlocking {
            try {
                Napier.i(
                    tag = LOGTAG,
                    message = "${session.method} ${session.uri}"
                )

                when(session.method) {
                    /**
                     * Allow cross-origin requests as per
                     * https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Methods/OPTIONS
                     */
                    Method.OPTIONS -> {
                        newFixedLengthResponse(
                            Response.Status.NO_CONTENT,
                            "application/json",
                            ByteArrayInputStream(byteArrayOf()),
                            0,
                        ).also {
                            it.addXapiCORSHeaders(session)
                            logResponse(session, it)
                        }
                    }

                    Method.GET -> {
                        val dataLoadState = xapiResourceProvider.provideXapiResource(
                            endpointUrl, authentication
                        ).statements.get(
                            listParams = XapiStatementsResource.GetStatementParams.fromParams(
                                params = StringValuesImpl(
                                    caseInsensitiveName = false,
                                    values = session.parameters
                                ),
                                json = json,
                            )
                        )

                        dataLoadState.toFixedLengthResponse(XapiStatementResult.serializer()).also {
                            it.addXapiCORSHeaders(session)
                            logResponse(session, it)
                        }
                    }

                    Method.POST -> {
                        val postBody = session.bodyAsBytes()?.let {
                            json.decodeFromString(
                                deserializer = XapiSingleItemToListSerializer,
                                string = it.decodeToString()
                            ).map { statement ->
                                statement.asAssignmentRecipeStmtIfIdNotNull(assignmentActivityId)
                            }
                        } ?: throw IllegalArgumentException("No Post Body")

                        xapiResourceProvider.provideXapiResource(
                            endpointUrl, authentication
                        ).statements.post(
                            list = postBody
                        ).toFixedLengthResponse(
                            ListSerializer(Uuid.serializer())
                        ).also {
                            logResponse(session, it)
                            it.addXapiCORSHeaders(session)
                        }
                    }

                    Method.PUT -> {
                        xapiResourceProvider.provideXapiResource(
                            endpointUrl, authentication
                        ).statements.put(
                            statementId = session.parameters["statementId"]?.first()?.let {
                                Uuid.parse(it)
                            } ?: throw IllegalArgumentException("Statements PUT requires statementId"),
                            statement = session.bodyAsBytes()?.decodeToString()?.let {
                                json.decodeFromString(XapiStatement.serializer(), it)
                            }?.asAssignmentRecipeStmtIfIdNotNull(assignmentActivityId)
                                ?: throw IllegalArgumentException("No body")
                        )

                        newFixedLengthResponse(
                            Response.Status.NO_CONTENT,
                            "application/json",
                            ByteArrayInputStream(byteArrayOf()),
                            0,
                        ).also {
                            it.addXapiCORSHeaders(session)
                            logResponse(session, it)
                        }
                    }

                    else -> {
                        newFixedLengthResponse(
                            Response.Status.METHOD_NOT_ALLOWED,
                            "text/plain",
                            ByteArrayInputStream(byteArrayOf()),
                            0,
                        ).also {
                            it.addXapiCORSHeaders(session)
                            logResponse(session, it)
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