package org.openeel.shared.viewmodel.person.qrcode

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.qr_code
import org.openeel.shared.navigation.QrCode
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.AppBarSearchUiState

data class InviteQrUiState(
    val link: String? = null,
    val schoolOrClass: String? = null
)

class InviteQrViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val route: QrCode = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(InviteQrUiState())
    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {
            it.copy(
                title = Res.string.qr_code.asUiText(),
                searchState = AppBarSearchUiState(visible = false),
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
        _uiState.value = InviteQrUiState(
            link = route.inviteLink,
            schoolOrClass = route.schoolOrClass
        )
    }
}
