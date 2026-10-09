package world.respect.shared.util.ext

import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText

fun String.asUiText(): UiText {
    return StringUiText(this)
}
