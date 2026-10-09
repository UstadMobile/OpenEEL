package world.respect.shared.ext

import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.LangMapObjectValue
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.libutil.util.selectLangOrNull
import kotlin.collections.get

fun LangMap.selectPreferredString(
    preferredLocales: List<String>
): String {
    return when(this) {
        is LangMapStringValue -> this.value
        is LangMapObjectValue -> {
            val langCodeToDisplay = selectLangOrNull(
                preferredLocales = preferredLocales,
                availableLocales = this.map.keys.toList(),
            )

            this.map[langCodeToDisplay] ?: "ERR: $langCodeToDisplay"
        }
    }
}