package world.respect.lib.xapi.nanohttpd.resources

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Response
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.nanohttpd.NanoHttpdXapiResponder
import world.respect.lib.xapi.nanohttpd.ext.addXapiCORSHeaders
import world.respect.lib.xapi.nanohttpd.ext.bodyAsXapiDocument
import world.respect.lib.xapi.nanohttpd.ext.headersAsKtorHeaders
import world.respect.lib.xapi.nanohttpd.ext.provideXapiResourceForSession
import world.respect.lib.xapi.nanohttpd.ext.toFixedLengthResponse
import world.respect.lib.xapi.nanohttpd.logResponse
import world.respect.lib.xapi.nanohttpd.newNoContentResponse
import world.respect.lib.xapi.resources.ISingleDocumentParams
import world.respect.lib.xapi.resources.XapiDocumentResource
import world.respect.lib.xapi.resources.XapiResource

abstract class AbstractXapiDocumentResourceResponder<
        MultiDocParams: Any,
        SingleDocParams: ISingleDocumentParams<MultiDocParams>,
        T: XapiDocumentResource<MultiDocParams, SingleDocParams>
>
(
    private val resourceProvider: XapiResourceProvider,
    private val json: Json,
): NanoHttpdXapiResponder {

    abstract fun XapiResource.documentResource(): T

    abstract fun NanoHTTPD.IHTTPSession.isMultiDocRequest(): Boolean

    abstract fun NanoHTTPD.IHTTPSession.getMultiDocParams(): MultiDocParams

    abstract fun NanoHTTPD.IHTTPSession.getSingleDocParams(): SingleDocParams

    override suspend fun serveXapiEndpoint(
        session: NanoHTTPD.IHTTPSession,
        pathSegments: List<String>
    ): Response {
        val xapiResource = resourceProvider.provideXapiResourceForSession(session)

        val response = when(session.method) {
            NanoHTTPD.Method.GET -> {
                if(session.isMultiDocRequest()) {
                    xapiResource.documentResource().getMultipleDocuments(
                        params = session.getMultiDocParams(),
                        dataLoadParams = DataLoadParams(
                            requestHeaders = session.headersAsKtorHeaders()
                        )
                    ).toFixedLengthResponse(
                        json, ListSerializer(String.serializer())
                    )
                }else {
                    xapiResource.documentResource().get(
                        params = session.getSingleDocParams(),
                        dataLoadParams = DataLoadParams(
                            requestHeaders = session.headersAsKtorHeaders()
                        )
                    ).toFixedLengthResponse(
                        dataReadyResponse = {
                            it.data.toFixedLengthResponse()
                        }
                    )
                }
            }

            NanoHTTPD.Method.POST -> {
                xapiResource.documentResource().post(
                    params = session.getSingleDocParams(),
                    document = session.bodyAsXapiDocument(),
                )

                newNoContentResponse()
            }

            NanoHTTPD.Method.PUT -> {
                xapiResource.documentResource().put(
                    params = session.getSingleDocParams(),
                    document = session.bodyAsXapiDocument(),
                )

                newNoContentResponse()
            }

            NanoHTTPD.Method.DELETE -> {
                xapiResource.documentResource().delete(
                    params = session.getSingleDocParams(),
                )

                newNoContentResponse()
            }

            else -> {
                newFixedLengthResponse(
                    NanoHTTPD.Response.Status.METHOD_NOT_ALLOWED,
                    NanoHTTPD.MIME_PLAINTEXT,
                    "Method not allowed"
                )
            }
        }

        return response.also {
            it.addXapiCORSHeaders(session)
            logResponse(session, it)
        }
    }
}