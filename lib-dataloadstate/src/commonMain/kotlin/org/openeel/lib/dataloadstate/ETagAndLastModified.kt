package world.respect.lib.dataloadstate

import kotlin.time.Instant

/**
 * Simple data class that holds an etag and last modified timestamp. This can be from response
 * headers (e.g. Etag and Last-Modified Http headers), from request headers (e.g. If-None-Match
 * and If-Modified-Since), or from the local database.
 *
 * @property etag etag string, if any, including the quotes
 * @property lastModified last modified timestamp, as an instant, if any
 */
data class ETagAndLastModified(
    val etag: String?,
    val lastModified: Instant?
)
