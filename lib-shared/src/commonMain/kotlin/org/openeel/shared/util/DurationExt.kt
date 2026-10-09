package world.respect.shared.util

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.duration_hours_short
import world.respect.shared.generated.resources.duration_minutes_short
import world.respect.shared.generated.resources.duration_seconds_short
import kotlin.time.Duration

@Composable
fun Duration.formatToShortString(): String {
    return toComponents { hours, minutes, seconds, _ ->
        buildString {
            if (hours > 0)
                append("$hours ${stringResource(Res.string.duration_hours_short)}:")

            append("${minutes}${stringResource(Res.string.duration_minutes_short)}: ")
            append(seconds.toString().padStart(2, '0'))
            append(stringResource(Res.string.duration_seconds_short))
        }
    }
}
