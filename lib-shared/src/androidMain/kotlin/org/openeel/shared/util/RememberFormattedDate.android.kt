package world.respect.shared.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import kotlinx.datetime.LocalDate
import android.text.format.DateFormat
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import java.util.Date

@Composable
actual fun rememberFormattedDate(date: LocalDate): String {
    val context = LocalContext.current

    val dateFormat: java.text.DateFormat = remember(Unit) {
        DateFormat.getDateFormat(context)
    }



    return remember(date) {
        val instant = LocalDateTime(date, LocalTime(0, 0))
            .toInstant(TimeZone.currentSystemDefault())

        dateFormat.format(
            Date(instant.toEpochMilliseconds())
        )
    }
}
