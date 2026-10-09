package world.respect.shared.viewmodel.app.appstate

import world.respect.shared.resources.UiText

/**
 * Represents the state of the action bar button e.g. the Save/Done button in the top right.
 * @property enabled when null the button will be enabled when it is visible and appuistate is not
 *           loading.
 *
 */
data class ActionBarButtonUiState(
    val visible: Boolean = false,
    val text: UiText? = null,
    val enabled: Boolean? = null,
    val onClick: () -> Unit = { },
)
