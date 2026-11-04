package org.openeel.shared.viewmodel.app.appstate

import org.openeel.shared.resources.UiText

data class Snack(
    val message: UiText,
    val action: UiText? = null,
    val onAction: (() -> Unit)? = null,
)
