package world.respect.lib.dataloadstate.datetime

import io.ktor.util.date.GMTDate
import kotlin.time.Instant

/**
 * Convert from GMTDate to an Instant.
 *
 * Note: if the GMTDate is not whole seconds it is deliberately rounded to seconds.
 */
fun GMTDate.toInstant(): Instant {
    return Instant.fromEpochMilliseconds(this.timestamp).roundToEpochSeconds()
}
