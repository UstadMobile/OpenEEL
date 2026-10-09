package world.respect.libutil.util.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

fun LocalDate.atStartOfDayInMillisUtc(): Long {
    return atStartOfDayIn(TimeZone.UTC).toEpochMilliseconds()
}

/**
 * Formats an Instant to a displayable date string (DD/MM/YYYY) in the current system timezone.
 */
fun Instant.toDisplayDateString(timeZone: TimeZone = TimeZone.currentSystemDefault()): String {
    return this.toLocalDateTime(timeZone).date.toDisplayDateString()
}

/**
 * Formats a LocalDate to a displayable date string (DD/MM/YYYY).
 */
fun LocalDate.toDisplayDateString(): String {
    val day = day.toString().padStart(2, '0')
    val month = month.number.toString().padStart(2, '0')
    return "$day/$month/$year"
}

fun String.toFormattedDate(): String = try {
    val dt = Instant.parse(this).toLocalDateTime(TimeZone.currentSystemDefault())
    "${dt.month.number}/${dt.day}/${dt.year}, ${
        dt.hour.toString().padStart(2, '0')
    }:${dt.minute.toString().padStart(2, '0')}"
} catch (e: Exception) {
    println("Date parsing failed: ${e.message}")
    this
}