package world.respect.lib.xapi.nanohttpd

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import java.io.ByteArrayInputStream

private val emptyByteArray = byteArrayOf()

fun newNoContentResponse() : NanoHTTPD.Response {
    return newFixedLengthResponse(
        NanoHTTPD.Response.Status.NO_CONTENT,
        "text/plain",
        ByteArrayInputStream(emptyByteArray),
        0,
    )
}
