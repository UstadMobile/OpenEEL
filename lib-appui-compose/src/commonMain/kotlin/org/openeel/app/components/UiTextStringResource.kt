package org.openeel.app.components

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.resources.LangMapUiText
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.resources.StringUiText
import org.openeel.shared.resources.UiText

@Composable
fun uiTextStringResource(uiText: UiText): String {
    return when(uiText) {
        is StringResourceUiText -> {
            stringResource(uiText.resource)
        }

        is LangMapUiText -> {
            langMapString(uiText.langMap)
        }

        is StringUiText -> uiText.text
    }
}
