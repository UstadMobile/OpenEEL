package org.openeel.shared.viewmodel.person.copycode

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import org.openeel.shared.domain.account.AppAccountManager
import org.openeel.shared.domain.clipboard.SetClipboardStringUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.code
import org.openeel.shared.navigation.CopyCode
import org.openeel.shared.util.ext.asUiText
import org.openeel.shared.viewmodel.OpenEelViewModel
import org.openeel.shared.viewmodel.app.appstate.AppBarSearchUiState

data class CopyInviteCodeUiState(
    val code: String? = null,
)

class CopyInviteCodeViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: AppAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase
) : OpenEelViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()
    private val route: CopyCode = savedStateHandle.toRoute()

    private val _uiState = MutableStateFlow(CopyInviteCodeUiState())
    val uiState = _uiState.asStateFlow()

    init {


        _uiState.update {
            it.copy(
                code = route.inviteCode,
            )
        }

        _appUiState.update {
            it.copy(
                title = Res.string.code.asUiText(),
                searchState = AppBarSearchUiState(visible = false),
                showBackButton = true,
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun copyCodeToClipboard() {
        _uiState.value.code?.also { setClipboardStringUseCase(it) }
    }

}