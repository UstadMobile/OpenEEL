package world.respect.datalayer.http.server.ext

import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.fromHttpToGmtDate
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respondBytes
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState
import world.respect.lib.xapi.exceptions.XapiException
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl

/**
 * Receive an XAPI document from the request. This will use receive<ByteArray> to get the contents
 * of the body and use the Content-Type and Last-Modified header to set type and updated
 * respectively.
 *
 * @receiver KTOR ApplicationCall
 * @return [XapiDocument] the xAPI document received from the request.
 */
suspend fun ApplicationCall.receiveXapiDocument() : XapiDocument {
    return XapiDocumentByteArrayImpl(
        type = request.headers[HttpHeaders.ContentType]
            ?: throw XapiException(400, "receiveXapiDocument: request has no content-type"),
        updated = request.headers[HttpHeaders.LastModified]?.fromHttpToGmtDate()
            ?: throw XapiException(400, "receiveXapiDocument: request has no last-modified"),
        contents = receive<ByteArray>(),
    )
}

suspend fun ApplicationCall.respondXapiDocument(
    document: DataLoadState<XapiDocument>,
) {
    //Note: DataLoadState metadata will have (and apply) the last-modified header
    respondDataLoadState(
        dataLoadState = document,
        onRespondWithData = { doc ->
            respondBytes(
                bytes = doc.contentsAsByteArray(),
                contentType = ContentType.parse(doc.type)
            )
        }
    )
}
