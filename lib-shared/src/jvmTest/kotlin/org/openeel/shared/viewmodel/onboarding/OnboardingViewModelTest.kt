package org.openeel.shared.viewmodel.onboarding

import androidx.lifecycle.SavedStateHandle
import com.russhwolf.settings.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.openeel.shared.domain.navigation.onappstart.NavigateOnAppStartUseCase
import org.openeel.shared.domain.onboarding.ShouldShowOnboardingUseCase
import org.openeel.shared.domain.usagereporting.GetUsageReportingEnabledUseCase
import org.openeel.shared.domain.usagereporting.SetUsageReportingEnabledUseCase
import org.openeel.shared.navigation.NavCommand
import org.openeel.shared.navigation.NewGetStartedScreen
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class OnboardingViewModelTest {

    @Test
    fun completingOnboardingPersistsAcrossInstancesAndKeepsNewGetStartedFlow(): Unit = runBlocking {
        val settings = mock<Settings>()
        var onboardingShown: String? = null
        whenever(settings.getStringOrNull(ShouldShowOnboardingUseCase.KEY_ONBOARDING_SHOWN))
            .thenAnswer { onboardingShown }
        doAnswer {
            onboardingShown = it.getArgument(1)
            null
        }.whenever(settings).putString(ShouldShowOnboardingUseCase.KEY_ONBOARDING_SHOWN, "true")
        val setUsageReporting = mock<SetUsageReportingEnabledUseCase>()
        val getUsageReporting = mock<GetUsageReportingEnabledUseCase>()
        whenever(getUsageReporting()).thenReturn(true)
        val viewModel = OnboardingViewModel(
            savedStateHandle = SavedStateHandle(),
            settings = settings,
            setUsageReportingEnabledUseCase = setUsageReporting,
            getUsageReportingEnabledUseCase = getUsageReporting,
            navigateOnAppStartUseCase = mock<NavigateOnAppStartUseCase>(),
        )
        assertTrue(ShouldShowOnboardingUseCase(settings)())

        viewModel.onToggleUsageStatsOptIn()
        viewModel.onClickGetStartedButton()

        verify(setUsageReporting).invoke(false)
        assertFalse(ShouldShowOnboardingUseCase(settings)())
        val navigation = assertIs<NavCommand.Navigate>(viewModel.navCommandFlow.first())
        assertIs<NewGetStartedScreen>(navigation.destination)
    }
}
