package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response

/**
 * Interface for NanoHttpdXapiResource respnoders
 */
interface NanoHttpdXapiResponder {

    suspend fun serveXapiEndpoint(
        session: IHTTPSession,
        pathSegments: List<String>,
    ): Response

}