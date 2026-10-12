package org.openeel.app.components

import androidx.compose.runtime.Composable
import org.openeel.lib.opds.model.LangMap
import org.openeel.lib.opds.model.LangMapObjectValue
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.libutil.util.selectLangOrNull

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
