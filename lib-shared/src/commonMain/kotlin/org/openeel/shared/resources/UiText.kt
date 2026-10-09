package world.respect.shared.resources

import org.jetbrains.compose.resources.StringResource

/**
 * Represents text that will be displayed in the user interface. This allows a ViewModel to update
 * UiState text using a localized resource without requiring coroutine scope.
 */
sealed class UiText

data class StringResourceUiText(val resource: StringResource): UiText()

data class StringUiText(val text: String): UiText()

data class LangMapUiText(val langMap: Map<String, String>): UiText()

