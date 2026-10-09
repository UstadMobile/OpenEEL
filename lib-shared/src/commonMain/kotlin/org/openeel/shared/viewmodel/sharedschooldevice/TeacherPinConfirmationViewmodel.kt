package world.respect.shared.viewmodel.sharedschooldevice

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.inject
import org.koin.core.scope.Scope
import world.respect.shared.domain.account.RespectAccountManager
import world.respect.shared.domain.account.sharedschooldevice.setpin.GetSharedDevicePINUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.invalid
import world.respect.shared.generated.resources.teacher_admin_login
import world.respect.shared.navigation.LoginScreen
import world.respect.shared.navigation.NavCommand
import world.respect.shared.resources.UiText
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.RespectViewModel

data class TeacherPinConfirmationUiState(
    val errorMessage: UiText? = null,
    val pin: String = "",
)

class TeacherPinConfirmationViewmodel(
    savedStateHandle: SavedStateHandle,
    private val accountManager: RespectAccountManager,
) : RespectViewModel(savedStateHandle), KoinScopeComponent {

    override val scope: Scope = accountManager.requireActiveAccountScope()

    private val getSharedDevicePINUseCase: GetSharedDevicePINUseCase by inject()


    private val _uiState = MutableStateFlow(TeacherPinConfirmationUiState())

    val uiState = _uiState.asStateFlow()


    init {
        _appUiState.update {
            it.copy(
                title = Res.string.teacher_admin_login.asUiText(),
                hideBottomNavigation = true,
                userAccountIconVisible = false
            )
        }
    }

    fun onPinChanged(pin: String) {
        _uiState.update { it.copy(pin = pin, errorMessage = null) }
    }

    fun onClickNext() {
        viewModelScope.launch {
            if (verifyTeacherPin(_uiState.value.pin)) {
                val schoolUrl = accountManager.activeAccount?.school?.self
                schoolUrl?.let { url ->
                    _navCommandFlow.tryEmit(
                        NavCommand.Navigate(LoginScreen.create(url, true))
                    )
                }
            } else {
                _uiState.update { it.copy(errorMessage = Res.string.invalid.asUiText()) }
            }
        }
    }

    private suspend fun verifyTeacherPin(enteredPin: String): Boolean {
        val correctPin = getSharedDevicePINUseCase()
        return enteredPin == correctPin
    }
}
