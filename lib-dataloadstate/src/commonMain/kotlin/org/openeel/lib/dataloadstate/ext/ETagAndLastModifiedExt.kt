package world.respect.lib.dataloadstate.ext

import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.headers
import io.ktor.http.toHttpDate
import world.respect.lib.dataloadstate.ETagAndLastModified
import world.respect.lib.dataloadstate.datetime.toGMTDate

/**
 * Create response headers that contain the ETag and Last-Modified header if the receiver's
 * etag and last-modified are non-null respectively.
 *
 * @receiver [ETagAndLastModified]
 * @return headers with ETag and Last-Modified set if the respective properties of the
 *         receiver are non-null.
 */
fun ETagAndLastModified.toResponseHeaders() : Headers {
    return headers {
        etag?.also {
            set(HttpHeaders.ETag, it)
        }

        lastModified?.also {
            set(HttpHeaders.LastModified, it.toGMTDate().toHttpDate())
        }
    }
}


/**
 * Validate a new ETagAndLastModified against a previously received ETagAndLastModified.
 *
 * If both an etag AND last-modified date are present, then the last-modified date will be ignored,
 * as per:
 * https://www.rfc-editor.org/info/rfc9110/#name-if-modified-since
 *
 * @receiver previously stored/received ETagAndLastModified
 * @param other ETagAndLastModified to validate against
 *
 * @return true if the receiver validates against the other ETagAndLastModified
 *
 */
fun ETagAndLastModified.isStillValid(
    other: ETagAndLastModified
) : Boolean{
    return if (etag != null) {
        other.etag == etag
    } else if (lastModified != null) {
        other.lastModified?.let { newLastMod ->
            newLastMod <= lastModified
        } == true
    } else {
        false
    }
}

/**
 * Compare only the last-modified times. Do not consider etags. Used to avoid overwriting newly
 * updated local data.
 *
 * @receiver [ETagAndLastModified] normally from a remote response
 * @param other [ETagAndLastModified] normally from a local response
 *
 * @return true if both the receiver and other parameter have non-null last-modified times and
 *         the receiver is newer than the other. False if both have a non-null last-modified time
 *         and the receiver is not newer than the other. Null if either last-modified time is null.
 */
fun ETagAndLastModified.isNewer(
    other: ETagAndLastModified
) : Boolean? {
    val otherLastMod = other.lastModified
    val thisLastMod = lastModified
    return if (otherLastMod != null && thisLastMod != null) {
        thisLastMod > otherLastMod
    } else {
        null
    }
}
