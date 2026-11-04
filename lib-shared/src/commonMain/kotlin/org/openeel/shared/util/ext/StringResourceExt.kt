package org.openeel.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.resources.UiText

fun StringResource.asUiText(): UiText {
    return StringResourceUiText(this)
}
