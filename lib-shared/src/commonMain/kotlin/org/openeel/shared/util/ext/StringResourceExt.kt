package world.respect.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText

fun StringResource.asUiText(): UiText {
    return StringResourceUiText(this)
}
