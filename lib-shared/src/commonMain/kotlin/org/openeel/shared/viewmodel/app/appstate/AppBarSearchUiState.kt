package world.respect.shared.viewmodel.app.appstate

/**
 * Represents the state of appbar search.
 */
data class AppBarSearchUiState (
    val visible: Boolean = false,
    val searchText: String = "",
    val onSearchTextChanged: (String) -> Unit = { },
)
