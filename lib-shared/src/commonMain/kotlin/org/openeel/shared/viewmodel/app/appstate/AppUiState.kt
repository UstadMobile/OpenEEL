package world.respect.shared.viewmodel.app.appstate

import world.respect.shared.resources.UiText

/**
 * @property showBackButton when null (default) the top left back button visibility is managed by
 *           sensible defaults (back button is hidden when on a root destination screen or when it
 *           is known the history stack does not have any previous destination). It may (rarely) be
 *           needed to explicitly set it on certain screens e.g. GetStarted which can be the very
 *           first screen the user sees (no back button) or it might come after clicking the add
 *           account button the Account List screen (back button is needed). Will have no effect on
 *           the web platform.
 * @property userAccountIconVisible when null (default) the profile icon will be shown unless the
 *           actionbar button is visible.
 */
data class AppUiState(
    val fabState: FabUiState = FabUiState(),
    val expandableFabState: ExpandableFabUiState = ExpandableFabUiState(),
    val title: UiText? = null,
    val navigationVisible: Boolean = true,
    val hideBottomNavigation: Boolean = false,
    val hideSettingsIcon: Boolean = false,
    val userAccountIconVisible: Boolean? = null,
    val hideAppBar: Boolean = false,
    val appBarColors: AppBarColors = AppBarColors.STANDARD,
    val actions: List<AppActionButton> = emptyList(),
    val loadingState: LoadingUiState = LoadingUiState(),
    val showBackButton: Boolean? = null,
    val searchState: AppBarSearchUiState = AppBarSearchUiState(),
    val actionBarButtonState: ActionBarButtonUiState = ActionBarButtonUiState(),
    val settingsIconVisible: Boolean? = null,
    val onClickSettings: (() -> Unit)? = null,
)