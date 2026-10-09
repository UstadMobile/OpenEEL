package world.respect.shared.util

import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate

@Composable
expect fun rememberFormattedDate(date: LocalDate): String
