package world.respect.shared.resources

import org.jetbrains.compose.resources.getString
import world.respect.libutil.util.selectStringOrNull


suspend fun getUiTextString(
    uiText: UiText,
    preferredLocales: List<String> = emptyList(),
): String {
    return when(uiText) {
        is StringUiText -> uiText.text
        is StringResourceUiText -> getString(uiText.resource)
        is LangMapUiText -> selectStringOrNull(
            langMap = uiText.langMap,
            preferredLocales = preferredLocales,
        ) ?: ""
    }
}
