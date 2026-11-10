package org.openeel.shared.viewmodel.catalog.opdsfeedshare

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.getString
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.clipboard.SetClipboardStringUseCase
import org.openeel.shared.domain.sharelink.CreatePlaylistShareLinkUseCase
import org.openeel.shared.domain.sharelink.LaunchSendEmailUseCase
import org.openeel.shared.domain.sharelink.LaunchSendSmsUseCase
import org.openeel.shared.domain.sharelink.LaunchShareLinkUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.share_collection
import org.openeel.shared.navigation.EnterLink
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.PlaylistShare
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.AppBarSearchUiState

data class OpdsFeedShareUiState(
    val playlistTitle: String = "",
    val shareUrl: String = "",
    val viewPermissionIndex: Int = VIEW_PERMISSION_DEFAULT_INDEX,
    val editPermissionIndex: Int = EDIT_PERMISSION_DEFAULT_INDEX,
) {
    companion object {
        const val VIEW_PERMISSION_DEFAULT_INDEX = 1
        const val EDIT_PERMISSION_DEFAULT_INDEX = 0
    }
}

class OpdsFeedShareViewModel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: AppAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase,
    private val shareLinkLauncher: LaunchShareLinkUseCase,
    private val smsLinkLauncher: LaunchSendSmsUseCase,
    private val launchSendEmailUseCase: LaunchSendEmailUseCase,
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val schoolDataSource: SchoolDataSource by inject()

    private val route: PlaylistShare = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(OpdsFeedShareUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.share_collection.asUiText(),
                searchState = AppBarSearchUiState(visible = false),
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }

        val activeAccount = accountManager.activeAccount
            ?: throw IllegalStateException(
                "No active account when initializing PlaylistShareViewModel"
            )

        val createPlaylistShareLinkUseCase = CreatePlaylistShareLinkUseCase(
            schoolUrl = activeAccount.school.self
        )

        val shareUrl = createPlaylistShareLinkUseCase(route.playlistUrl).toString()
        _uiState.update { it.copy(shareUrl = shareUrl) }

        viewModelScope.launch {
            schoolDataSource.opdsFeedDataSource.getByUrlAsFlow(
                url = route.playlistUrl,
                params = DataLoadParams(),
            ).collect { result ->
                when (result) {
                    is DataReadyState -> {
                        _uiState.update {
                            it.copy(playlistTitle = result.data.metadata.title)
                        }
                    }
                    else -> { /* loading/error handled by app bar loading indicator */ }
                }
            }
        }
    }

    fun onViewPermissionChanged(index: Int) {
        _uiState.update { it.copy(viewPermissionIndex = index) }
    }

    fun onEditPermissionChanged(index: Int) {
        _uiState.update { it.copy(editPermissionIndex = index) }
    }

    fun onClickCopyLink() {
        setClipboardStringUseCase(_uiState.value.shareUrl)
        _navCommandFlow.tryEmit(NavCommand.Navigate(EnterLink))
    }

    fun onClickShareLink() {
        viewModelScope.launch {
            shareLinkLauncher(_uiState.value.shareUrl)
        }
    }

    fun onClickSendViaSms() {
        viewModelScope.launch {
            smsLinkLauncher(_uiState.value.shareUrl)
        }
    }

    fun onClickSendViaEmail() {
        viewModelScope.launch {
            launchSendEmailUseCase(
                LaunchSendEmailUseCase.LaunchSendEmailRequest(
                    subject = getString(Res.string.share_collection),
                    body = _uiState.value.shareUrl,
                    to = null,
                )
            )
        }
    }
}