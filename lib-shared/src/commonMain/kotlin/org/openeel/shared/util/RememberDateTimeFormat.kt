package world.respect.shared.util

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFormattedDateTime(
    timeInMillis: Long,
    timeZoneId: String,
    joinDateAndTime: (
        @ParameterName(name = "date") String,
        @ParameterName(name = "time") String
    ) -> String = { date, time ->
        "$date $time"
    },
): String
