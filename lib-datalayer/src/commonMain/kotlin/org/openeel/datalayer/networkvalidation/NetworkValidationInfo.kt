package world.respect.datalayer.networkvalidation

import kotlin.time.Instant

/**
 * Validation info needed when making an http datasource implementation is about to make a request
 *
 * @param lastModified as per the last-modified http header
 * @param etag as per the etag http header
 * @param consistentThrough X-Consistent-Through header as per the xAPI spec. Used to set since
 *        parameter in the request to load only entities that have changed since the last check.
 * @param lastChecked when this validation info was last checked (e.g. if a request was sent and a
 *        not modified response was received).
 */
data class NetworkValidationInfo(
    val lastModified: Long,
    val etag: String?,
    val consistentThrough: Instant? = null,
    val lastChecked: Long = 0,
    val varyHeader: String? = null,
    val validationInfoKey: Long = 0,
)
