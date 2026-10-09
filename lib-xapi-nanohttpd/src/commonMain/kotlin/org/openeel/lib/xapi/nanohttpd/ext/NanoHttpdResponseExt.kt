package world.respect.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import io.ktor.http.Url
import io.ktor.http.protocolWithAuthority
import io.ktor.util.StringValues

/**
 * Add cross-origin headers.
 *
 * See https://developer.mozilla.org/en-US/docs/Web/HTTP/Reference/Headers/Access-Control-Allow-Origin
 */
fun NanoHTTPD.Response.addXapiCORSHeaders(
    session: NanoHTTPD.IHTTPSession
) {
    val origin = session.headers["origin"] ?: session.headers["referer"]
        ?: return
    val originUrl = Url(origin).protocolWithAuthority

    addHeader("Access-Control-Allow-Origin", originUrl)

    session.headers["access-control-request-method"]?.also { requestMethods ->
        addHeader("Access-Control-Allow-Methods", requestMethods)
    }

    session.headers["access-control-request-headers"]?.also { requestHeaders ->
        addHeader("Access-Control-Allow-Headers", requestHeaders)
    }
}

fun NanoHTTPD.Response.addHeaders(
    headers: StringValues
) {
    headers.forEach { key, values ->
        values.forEach { value ->
            addHeader(key, value)
        }
    }
}
