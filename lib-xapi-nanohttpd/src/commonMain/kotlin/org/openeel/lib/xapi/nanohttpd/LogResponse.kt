package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD.IHTTPSession
import fi.iki.elonen.NanoHTTPD.Response
import io.github.aakira.napier.Napier
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp.Companion.LOGTAG

internal fun logResponse(
    session: IHTTPSession,
    response: Response
) {
    Napier.i(
        tag = LOGTAG,
        message = "HTTP ${response.status.requestStatus}: ${session.method} ${session.uri}"
    )
}