package org.openeel.shared.viewmodel.onboarding

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.openeel.shared.viewmodel.OpenEelViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.update
import org.openeel.shared.domain.onboarding.ShouldShowOnboardingUseCase
import org.openeel.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import org.openeel.shared.domain.usagereporting.SetUsageReportingEnabledUseCase
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NewGetStartedScreen

data class OnboardingUiState(
    val isLoading: Boolean = false,
    val usageStatsOptInChecked: Boolean = true,
)

class OnboardingViewModel(
    savedStateHandle: SavedStateHandle,
    private val settings: Settings,
    private val setUsageReportingEnabledUseCase: SetUsageReportingEnabledUseCase,
    private val getUsageReportingEnabledUseCase: GetUsageReportingEnabledUseCase,
) : OpenEelViewModel(savedStateHandle) {

    private val _uiState = MutableStateFlow(OnboardingUiState())

    val uiState = _uiState.asStateFlow()

    init {
        _appUiState.update { prev ->
            prev.copy(
                hideBottomNavigation = true,
                hideAppBar = true
            )
        }
        _uiState.update { it.copy(usageStatsOptInChecked = getUsageReportingEnabledUseCase()) }
    }

    fun onToggleUsageStatsOptIn() {
        _uiState.update { prev ->
            prev.copy(
                usageStatsOptInChecked = !prev.usageStatsOptInChecked
            )
        }
    }

    fun onClickGetStartedButton() {
        setUsageReportingEnabledUseCase(_uiState.value.usageStatsOptInChecked)
        settings.putString(ShouldShowOnboardingUseCase.KEY_ONBOARDING_SHOWN, true.toString())
        _navCommandFlow.tryEmit(
            NavCommand.Navigate(
                destination = NewGetStartedScreen(),
                clearBackStack = true,
            )
        )

    }

}