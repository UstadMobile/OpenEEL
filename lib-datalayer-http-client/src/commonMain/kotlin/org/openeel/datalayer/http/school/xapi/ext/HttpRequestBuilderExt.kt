package world.respect.datalayer.http.school.xapi.ext

import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.header
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.http.toHttpDate
import world.respect.lib.xapi.model.XapiDocument

/**
 * Set the given XapiDocument as the body. This will:
 *  a) set the Last-Modified header
 *  b) set the Content-Type header
 *  c) Set the body to use contents of the document (from its byte array)
 */
suspend fun HttpRequestBuilder.setXapiDocumentBody(
    document: XapiDocument
) {
    header(
        key = HttpHeaders.LastModified,
        value = document.updated.toHttpDate()
    )
    contentType(ContentType.parse(document.type))
    setBody(document.contentsAsByteArray())
}
