package world.respect.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiResource

suspend fun XapiResourceProvider.provideXapiResourceForSession(
    session: NanoHTTPD.IHTTPSession
): XapiResource {
    return provideXapiResource(
        endpoint = session.endpointUrl(),
        authentication = session.headers["authorization"]
    )
}
