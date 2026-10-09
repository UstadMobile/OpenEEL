package world.respect.shared.viewmodel.app.appstate

import world.respect.shared.resources.UiText

data class ExpandableFabUiState(
    val visible: Boolean = false,
    val expanded: Boolean = false,
    val text: UiText? = null,
    val items: List<ExpandableFabItem> = emptyList()
)

data class ExpandableFabItem(
    val text: UiText,
    val icon: ExpandableFabIcon,
    val onClick: () -> Unit = {}
)
enum class ExpandableFabIcon {
    ADD,
    INVITE,
    LINK,
}