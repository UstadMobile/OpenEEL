package world.respect.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.text.SimpleDateFormat
import java.util.Date

@Composable
actual fun rememberFormattedDate(date: LocalDate): String {
    val simpleDateFormat = remember(Unit) {
        SimpleDateFormat.getDateInstance()
    }

    return remember(date) {
        val instant = LocalDateTime(date, LocalTime(0, 0))
            .toInstant(TimeZone.currentSystemDefault())
        simpleDateFormat.format(Date(instant.toEpochMilliseconds()))
    }
}