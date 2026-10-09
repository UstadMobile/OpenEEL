package world.respect.shared.viewmodel.app.appstate

import world.respect.shared.resources.UiText

data class Snack(
    val message: UiText,
    val action: UiText? = null,
    val onAction: (() -> Unit)? = null,
)
