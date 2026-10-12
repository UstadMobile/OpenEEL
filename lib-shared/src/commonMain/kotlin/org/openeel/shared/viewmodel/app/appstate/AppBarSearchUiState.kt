package org.openeel.shared.viewmodel.app.appstate

/**
 * Represents the state of appbar search.
 *
 * @property expanded true when the search input is shown (e.g. after the user clicks the search
 *           icon). Screens use this to show search related options such as filters.
 */
data class AppBarSearchUiState (
    val visible: Boolean = false,
    val expanded: Boolean = false,
    val searchText: String = "",
    val onSearchTextChanged: (String) -> Unit = { },
    val onSearchExpandedChanged: (Boolean) -> Unit = { },
)
