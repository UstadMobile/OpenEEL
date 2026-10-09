package world.respect.lib.primarykeygen.ext

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Returns the number of milliseconds until the next second starts.
 */
@OptIn(ExperimentalTime::class)
val Instant.millisUntilNextSecond : Int
    get() = nanosecondsOfSecond / 1_000_000