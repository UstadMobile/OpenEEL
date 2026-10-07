package org.openeel.shared.util.ext

import org.openeel.shared.resources.StringUiText
import org.openeel.shared.resources.UiText

fun String.asUiText(): UiText {
    return StringUiText(this)
}
