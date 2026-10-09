package world.respect.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.newFixedLengthResponse
import world.respect.lib.xapi.model.XapiDocument
import java.io.ByteArrayInputStream

suspend fun XapiDocument.toFixedLengthResponse(): NanoHTTPD.Response {
    val byteArr = contentsAsByteArray()

    return newFixedLengthResponse(
        NanoHTTPD.Response.Status.OK,
        type,
        ByteArrayInputStream(byteArr),
        byteArr.size.toLong()
    )
}