package org.openeel.shared.ext

import org.openeel.lib.opds.model.LangMap
import org.openeel.lib.opds.model.LangMapObjectValue
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.libutil.util.selectLangOrNull
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