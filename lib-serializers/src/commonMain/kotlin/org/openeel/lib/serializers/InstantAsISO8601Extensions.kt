package world.respect.lib.serializers


import kotlin.time.Duration.Companion.milliseconds

fun InstantAsISO8601.plusMillis(
    millis: Long
): InstantAsISO8601 =
    this + millis.milliseconds
