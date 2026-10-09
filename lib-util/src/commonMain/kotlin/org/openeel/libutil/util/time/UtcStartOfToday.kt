package world.respect.libutil.util.time

import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Clock

/**
 * A LocalDate field includes only the day, month, and year. It does not include a time or timezone.
 *
 * When LocalDate fields are stored in the database, they are stored as milliseconds since epoch
 * until the given date at 00:00:00 UTC (see SchoolTypeConverters.fromLocalDate) .
 *
 * To make comparisons (e.g. is an enrollment active today), we need to know how many milliseconds
 * since utc until today started. Today might be a different date depending on the timezone.
 *
 * To get a sensible default value : we take the now Instant, get a local date using the system
 * default timezone, and then convert that to millis since epoch for the start of the day in UTC.
 */
fun startOfTodaysDateInMillisAtUtc() : Long{
    return Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.atStartOfDayIn(
        TimeZone.UTC
    ).toEpochMilliseconds()
}