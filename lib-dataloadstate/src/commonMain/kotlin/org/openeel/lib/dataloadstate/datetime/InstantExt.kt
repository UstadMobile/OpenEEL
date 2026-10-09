package world.respect.lib.dataloadstate.datetime

import io.ktor.util.date.GMTDate
import kotlin.time.Instant

/**
 * Round off an Instant to the nearest epoch second.
 * HTTP last-modified timestamps are rounded off to the nearest second, so any data that relies on
 * the last-modified field must NOT attempt to use millisecond precision.
 */
fun Instant.roundToEpochSeconds(): Instant {
    return if(nanosecondsOfSecond > 0) {
        Instant.fromEpochSeconds(epochSeconds)
    }else {
        this
    }
}

/**
 * Convert this Instant to a GMTDate.
 *
 * Important: This will result in loss of precision. GMTDate DOES NOT support millisecond precision.
 */
fun Instant.toGMTDate(): GMTDate {
    return GMTDate(timestamp = this.epochSeconds * 1000)
}
