package org.openeel.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.resources.XapiResource

suspend fun XapiResourceProvider.provideXapiResourceForSession(
    session: NanoHTTPD.IHTTPSession
): XapiResource {
    return provideXapiResource(
        endpoint = session.endpointUrl(),
        authentication = session.headers["authorization"]
    )
}
