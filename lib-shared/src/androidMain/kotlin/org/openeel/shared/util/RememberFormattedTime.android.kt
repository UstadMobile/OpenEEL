package world.respect.shared.util

import android.text.format.DateFormat
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import java.util.Date
import java.util.TimeZone
import kotlin.time.Instant

@Composable
actual fun rememberFormattedTime(instant: Instant) : String {
    val context = LocalContext.current
    val timeFormatter = remember(Unit) {
        DateFormat.getTimeFormat(context)
    }

    return remember(instant) {
        timeFormatter.format(Date(instant.toEpochMilliseconds()))
    }
}