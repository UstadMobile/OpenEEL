package world.respect.shared.util

import androidx.compose.runtime.Composable
import kotlin.time.Instant

@Composable
expect fun rememberFormattedTime(instant: Instant): String