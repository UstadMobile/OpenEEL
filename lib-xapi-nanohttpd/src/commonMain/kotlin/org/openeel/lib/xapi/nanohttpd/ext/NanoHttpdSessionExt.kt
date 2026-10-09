package world.respect.lib.xapi.nanohttpd.ext

import fi.iki.elonen.NanoHTTPD
import fi.iki.elonen.NanoHTTPD.Method
import io.ktor.http.Headers
import io.ktor.http.HeadersImpl
import io.ktor.http.Parameters
import io.ktor.http.ParametersImpl
import io.ktor.http.Url
import io.ktor.http.fromHttpToGmtDate
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import world.respect.lib.dataloadstate.datetime.toGMTDate
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp.Companion.ENDPOINT_SEGMENT_INDEX
import java.io.File
import kotlin.time.Clock

fun NanoHTTPD.IHTTPSession.bodyAsBytes(): ByteArray? {
    val bodyMap = mutableMapOf<String,String>()
    parseBody(bodyMap)

    return if(method == Method.PUT) {
        //NanoHTTPD will always put the content of a PUT body into a temp file, with the path in the "content" key
        val tmpFileName = bodyMap["content"]
        tmpFileName?.let { File(it).readBytes() }
    }else if(method == Method.POST) {
        //NanoHTTPD will put small (less than 1024 bytes) content into the memory, otherwise it will make a file
        val mapContent = bodyMap["postData"] ?: return null
        val tmpFile = File(mapContent)
        if(tmpFile.exists()) {
            tmpFile.readBytes()
        }else {
            mapContent.encodeToByteArray()
        }
    }else {
        null
    }
}

/**
 * Get the endpoint URL for this request - see [world.respect.lib.xapi.nanohttpd.XapiNanoHttpdApp.localUrlForEndpoint]
 */
fun NanoHTTPD.IHTTPSession.endpointUrl(): Url {
    //Remove the first slash, then split into path segments. Because we are looking for the second
    //segment, the split limit is 3.
    val pathSegments = uri.substring(1).split("/", limit = 3)
    return Url(UrlEncoderUtil.decode(
        pathSegments[ENDPOINT_SEGMENT_INDEX])
    )
}

fun NanoHTTPD.IHTTPSession.headersAsKtorHeaders(): Headers {
    return HeadersImpl(headers.map { it.key to listOf(it.value) }.toMap())
}

/**
 * Convert the query parameters from the receiver IHTTPSession to Ktor
 * parameters.
 */
fun NanoHTTPD.IHTTPSession.parametersAsKtorParams(): Parameters {
    return ParametersImpl(parameters.toMap())
}

fun NanoHTTPD.IHTTPSession.bodyAsXapiDocument(): XapiDocument {
    return XapiDocumentByteArrayImpl(
        type = headers["content-type"]
            ?: throw XapiException(400, "bodyAsXapiDocument: request has no content-type"),
        updated = headers["last-modified"]?.fromHttpToGmtDate() ?: Clock.System.now().toGMTDate(),
        contents = bodyAsBytes()
            ?: throw XapiException(400, "bodyAsXapiDocument: request has no body"),
    )
}