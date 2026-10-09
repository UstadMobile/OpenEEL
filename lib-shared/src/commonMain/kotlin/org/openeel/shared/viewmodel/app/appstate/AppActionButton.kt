package world.respect.shared.viewmodel.app.appstate

import world.respect.shared.resources.UiText

/**
 * @param id Used to set the DOM id for the action button on React. Sets the testTag on Compose.
 */
data class AppActionButton(
    val icon: AppStateIcon,
    val contentDescription: UiText,
    val text: UiText,
    val onClick: () -> Unit,
    val id: String,
    val display: ActionButtonDisplay = ActionButtonDisplay.ICON,
) {
    companion object {
        enum class ActionButtonDisplay {
            ICON,
            OVERFLOW_MENU
        }
    }
}
