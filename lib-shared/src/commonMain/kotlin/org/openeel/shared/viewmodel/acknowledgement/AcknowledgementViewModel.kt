package world.respect.shared.viewmodel.acknowledgement

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import world.respect.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import world.respect.shared.domain.onboarding.ShouldShowOnboardingUseCase
import world.respect.shared.navigation.NavCommand
import world.respect.shared.navigation.Onboarding
import world.respect.shared.viewmodel.RespectViewModel

data class AcknowledgementUiState(
    val isLoading: Boolean = false,
)
class AcknowledgementViewModel(
    savedStateHandle: SavedStateHandle,
    private val navigateOnAppStartUseCase: NavigateOnAppStartUseCase,
    private val shouldShowOnboardingUseCase: ShouldShowOnboardingUseCase,
) : RespectViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(AcknowledgementUiState())

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _appUiState.update { prev ->
                prev.copy(
                    hideBottomNavigation = true,
                    hideAppBar = true
                )
            }

            delay(2000)

            _navCommandFlow.tryEmit(
                value = if(shouldShowOnboardingUseCase()) {
                    NavCommand.Navigate(
                        destination = Onboarding,
                        clearBackStack = true,
                    )
                }else {
                    navigateOnAppStartUseCase()
                }
            )
        }
    }

}
