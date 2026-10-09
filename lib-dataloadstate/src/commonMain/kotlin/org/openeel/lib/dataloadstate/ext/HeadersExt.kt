package world.respect.lib.dataloadstate.ext

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.fromHttpToGmtDate
import world.respect.lib.dataloadstate.ETagAndLastModified
import world.respect.lib.dataloadstate.datetime.toInstant

/**
 * Simple short function to check if an incoming request has cache validation parameters.
 */
fun Headers.hasIfNotModifiedHeaders() : Boolean {
    return this[HttpHeaders.IfNoneMatch] != null || this[HttpHeaders.IfModifiedSince] != null
}

/**
 * Gets the Etag and Last-Modified to be validated from request headers; the If-None-Match and
 * If-Modified-Since headers.
 *
 * @receiver Headers from a request
 * @return [ETagAndLastModified] based on the If-None-Match and If-Modified-Since headers that
 *         the request is validating against.
 */
fun Headers.requestEtagAndLastModified(): ETagAndLastModified {
    return ETagAndLastModified(
        etag = this[HttpHeaders.IfNoneMatch],
        lastModified = this[HttpHeaders.IfModifiedSince]?.fromHttpToGmtDate()?.toInstant()
    )
}

/**
 * Gets the Etag and Last-Modified from Response Headers (the Last-Modified and ETag headers)
 * if present.
 *
 * @receiver Headers from a response
 * @return [ETagAndLastModified] based on the etag and Last-Modified as available.
 */
fun Headers.responseETagAndLastModified(): ETagAndLastModified {
    return ETagAndLastModified(
        etag = this[HttpHeaders.ETag],
        lastModified = this[HttpHeaders.LastModified]?.fromHttpToGmtDate()?.toInstant()
    )
}

