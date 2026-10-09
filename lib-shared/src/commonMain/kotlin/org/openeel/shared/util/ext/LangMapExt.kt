package world.respect.shared.util.ext

import world.respect.lib.opds.model.LangMap
import world.respect.lib.opds.model.LangMapObjectValue
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.shared.resources.LangMapUiText
import world.respect.shared.resources.UiText

fun Map<String, String>.asLangMapUiText(): UiText = LangMapUiText(this)

fun LangMap.asUiText(): UiText = when(this) {
    is LangMapStringValue -> value.asUiText()
    is LangMapObjectValue -> LangMapUiText(map)
}

