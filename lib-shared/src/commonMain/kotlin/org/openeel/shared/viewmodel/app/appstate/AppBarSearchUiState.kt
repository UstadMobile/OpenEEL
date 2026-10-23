package org.openeel.shared.viewmodel.app.appstate

/**
 * Represents the state of appbar search.
 *
 * @property expandedByDefault when true, the search box is shown open when the screen is opened.
 */
data class AppBarSearchUiState (
    val visible: Boolean = false,
    val expandedByDefault: Boolean = false,
    val searchText: String = "",
    val onSearchTextChanged: (String) -> Unit = { },
)
