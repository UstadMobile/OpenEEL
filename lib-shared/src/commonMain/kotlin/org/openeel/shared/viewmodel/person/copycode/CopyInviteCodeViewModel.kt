package world.respect.shared.viewmodel.person.copycode

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.clipboard.SetClipboardStringUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.code
import world.respect.shared.navigation.CopyCode
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel
import world.respect.shared.viewmodel.app.appstate.AppBarSearchUiState

data class CopyInviteCodeUiState(
    val code: String? = null,
)

class CopyInviteCodeViewModel(
    savedStateHandle: SavedStateHandle,
    accountManager: RespectAccountManager,
    private val setClipboardStringUseCase: SetClipboardStringUseCase
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

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