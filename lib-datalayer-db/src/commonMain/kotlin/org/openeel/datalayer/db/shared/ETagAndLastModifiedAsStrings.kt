package world.respect.datalayer.db.shared

import world.respect.lib.dataloadstate.ETagAndLastModified

/**
 * Sometimes database resources will run a quick validation check query to see if the response has
 * bene modified (as per the IfModifiedSince and IfNoneMatch request headers). The database default
 * handling of the Instant class uses a Long (not consistent with SQL LRS schema used on newer tables).
 *
 * This data class provides a return type for the ETag and LastModified where the LastModified is
 * stored as a string.
 */
data class ETagAndLastModifiedAsStrings(
    val etag: String?,
    val lastModified: InstantAsTimestampString?
)

fun ETagAndLastModifiedAsStrings.toModel(): ETagAndLastModified {
    return ETagAndLastModified(
        etag = etag,
        lastModified = lastModified?.instant
    )
}
