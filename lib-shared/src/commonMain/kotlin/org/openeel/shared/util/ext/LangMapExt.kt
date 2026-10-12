package org.openeel.shared.util.ext

import org.openeel.lib.opds.model.LangMap
import org.openeel.lib.opds.model.LangMapObjectValue
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.shared.resources.LangMapUiText
import org.openeel.shared.resources.UiText

fun Map<String, String>.asLangMapUiText(): UiText = LangMapUiText(this)

fun LangMap.asUiText(): UiText = when(this) {
    is LangMapStringValue -> value.asUiText()
    is LangMapObjectValue -> LangMapUiText(map)
}

