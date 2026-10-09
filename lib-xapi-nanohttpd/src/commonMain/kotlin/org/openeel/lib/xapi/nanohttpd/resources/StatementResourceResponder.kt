package world.respect.lib.xapi.nanohttpd.resources

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import world.respect.lib.xapi.nanohttpd.ext.parametersAsKtorParams
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.xapi.OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.ext.asAssignmentRecipeStmtIfIdNotNull
import world.respect.lib.xapi.ext.put
import world.respect.lib.xapi.model.XapiSingleItemToListSerializer
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiStatementResult
import world.respect.lib.xapi.nanohttpd.NanoHttpdXapiResponder
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp.Companion.ENDPOINT_SEGMENT_INDEX
import world.respect.lib.xapi.nanohttpd.ext.addXapiCORSHeaders
import world.respect.lib.xapi.nanohttpd.ext.bodyAsBytes
import world.respect.lib.xapi.nanohttpd.ext.provideXapiResourceForSession
import world.respect.lib.xapi.nanohttpd.ext.toFixedLengthResponse
import world.respect.lib.xapi.nanohttpd.logResponse
import world.respect.lib.xapi.resources.XapiStatementsResource
import java.io.ByteArrayInputStream
import kotlin.uuid.Uuid

class StatementResourceResponder(
    private val resourceProvider: XapiResourceProvider,
    private val json: Json,
): NanoHttpdXapiResponder {

    override suspend fun serveXapiEndpoint(
        session: NanoHTTPD.IHTTPSession,
        pathSegments: List<String>
    ): NanoHTTPD.Response {
        val xapiResource = resourceProvider.provideXapiResourceForSession(session)

        val nextSegment = pathSegments[ENDPOINT_SEGMENT_INDEX + 1]

        val assignmentXform = nextSegment == ASSIGNMENT_XAPI_SEGMENT
        val assignmentActivityId = if(assignmentXform) {
            UrlEncoderUtil.decode(pathSegments[ENDPOINT_SEGMENT_INDEX + 2])
        }else {
            null
        }

        return when(session.method) {
            Method.GET -> {
                xapiResource.statements.get(
                    listParams = XapiStatementsResource.GetStatementParams.fromParams(
                        params = session.parametersAsKtorParams(),
                        json = json,
                    )
                ).toFixedLengthResponse(
                    json, XapiStatementResult.serializer()
                ).also {
                    it.addXapiCORSHeaders(session)
                    logResponse(session, it)
                }
            }

            Method.POST -> {
                xapiResource.statements.post(
                    list = session.bodyAsBytes()?.let { bodyBytes ->
                        json.decodeFromString(
                            deserializer = XapiSingleItemToListSerializer,
                            string = bodyBytes.decodeToString()
                        ).map { statement ->
                            statement.asAssignmentRecipeStmtIfIdNotNull(assignmentActivityId)
                        }
                    } ?: throw IllegalArgumentException("No Post Body")
                ).toFixedLengthResponse(
                    json, ListSerializer(Uuid.serializer())
                ).also {
                    logResponse(session, it)
                    it.addXapiCORSHeaders(session)
                }
            }

            Method.PUT -> {
                xapiResource.statements.put(
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
    }
}