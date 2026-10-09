package world.respect.datalayer.db.shared

import kotlin.time.Instant

/**
 * Wrapper used to tell TypeConverters that the value is an Instant and should be converted to/from
 * a string when writing to/reading from the database (others convert to/from millis).
 */
data class InstantAsTimestampString(
    val instant: Instant
)
