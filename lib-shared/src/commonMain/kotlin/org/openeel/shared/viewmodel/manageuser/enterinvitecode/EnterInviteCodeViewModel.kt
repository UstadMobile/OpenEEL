package world.respect.shared.viewmodel.manageuser.enterinvitecode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.domain.account.invite.GetInviteInfoUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_code_label
import world.respect.shared.generated.resources.invalid_invite_code
import world.respect.shared.generated.resources.something_went_wrong
import world.respect.shared.navigation.AcceptInvite
import world.respect.shared.navigation.EnterInviteCode
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText
import world.respect.shared.util.di.SchoolDirectoryEntryScopeId
import world.respect.shared.util.exception.getUiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class EnterInviteCodeUiState(
    val inviteCode: String = "",
    val errorMessage:  UiText? = null,
)

class EnterInviteCodeViewModel(
    savedStateHandle: SavedStateHandle,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    val route: EnterInviteCode = savedStateHandle.toRoute()

    override val scope: Scope
        get() = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
            SchoolDirectoryEntryScopeId(route.schoolUrl, null).scopeId
        )

    private val getInviteInfoUseCase: GetInviteInfoUseCase by inject()

    private val _uiState = MutableStateFlow(EnterInviteCodeUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update {prev ->
            prev.copy(
                title = Res.string.enter_code_label.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false,
            )
        }
    }

    fun onCodeChanged(code: String) {
        _uiState.update {
            it.copy(
                inviteCode = code,
                errorMessage = null
            )
        }
    }

    fun onClickNext() {
        viewModelScope.launch {
            if (uiState.value.inviteCode.isBlank()) {
                _uiState.update {
                    it.copy(errorMessage = StringResourceUiText(Res.string.invalid_invite_code))
                }
                return@launch
            }
            try {
                val inviteCode = uiState.value.inviteCode.trim()
                getInviteInfoUseCase(inviteCode)

                _navCommandFlow.tryEmit(
                    NavCommand.Navigate(
                        AcceptInvite.create(
                            schoolUrl = route.schoolUrl,
                            code = inviteCode
                        )
                    )
                )
            }catch(e: Exception) {
                e.printStackTrace()
                _uiState.update { prev ->
                    prev.copy(
                        errorMessage = e.getUiText() ?: StringResourceUiText(Res.string.something_went_wrong)
                    )
                }
            }
        }
    }
}
