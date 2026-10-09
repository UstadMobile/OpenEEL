package world.respect.app.components

import androidx.compose.runtime.Composable
import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.LangMapObjectValue
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.libutil.util.selectLangOrNull

@Composable
fun langMapString(
    langMap: Map<String, String>
): String {
    val langCodeToDisplay = selectLangOrNull(
        preferredLocales = listOf(LocalAppLocale.current),
        availableLocales = langMap.keys.toList(),
    )
    return langMap[langCodeToDisplay] ?: "ERR: $langCodeToDisplay"
}

@Composable
fun langMapString(
    langMap: LangMap
): String {
    return when(langMap) {
        is LangMapStringValue -> langMap.value
        is LangMapObjectValue -> langMapString(langMap.map)
    }
}
