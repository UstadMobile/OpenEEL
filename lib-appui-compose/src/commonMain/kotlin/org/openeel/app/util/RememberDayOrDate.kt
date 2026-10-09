package world.respect.app.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.daysUntil
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.today
import world.respect.shared.generated.resources.yesterday
import java.text.DateFormat
import java.util.Date
import kotlin.time.Instant

/**
 * Returns a short "when" for the given timestamp as follows:
 *
 * If timestamp is from the same day
 *   If showTimeIfToday=false return "today" localized string
 *   If showTimeIfToday=true return formatted time
 * If timestamp is from yesterday, return "yesterday"
 * If timestamp is within the last week, return the day of the week
 * Otherwise, return formatted date
 */
@Composable
fun rememberDayOrDate(
    localDateTimeNow: LocalDateTime,
    timestamp: Long,
    timeZone: TimeZone,
    showTimeIfToday: Boolean,
    timeFormatter: DateFormat,
    dateFormatter: DateFormat,
    dayOfWeekStringMap: Map<DayOfWeek, String>,
): String {
    val todayStr = stringResource(Res.string.today)
    val yesterdayStr = stringResource(Res.string.yesterday)

    return remember(timestamp, localDateTimeNow) {
        val timestampInstant = Instant.fromEpochMilliseconds(timestamp)
        val timestampLocalDateTime = timestampInstant.toLocalDateTime(timeZone)
        val epochDaysPassed = timestampLocalDateTime.date.daysUntil(localDateTimeNow.date)
        when {
            epochDaysPassed == 0 -> if (showTimeIfToday) {
                timeFormatter.format(Date(timestamp))
            } else {
                todayStr
            }

            epochDaysPassed == 1 -> yesterdayStr
            epochDaysPassed <= 7 -> dayOfWeekStringMap[timestampLocalDateTime.dayOfWeek] ?: ""
            else -> dateFormatter.format(Date(timestamp))
        }
    }
}