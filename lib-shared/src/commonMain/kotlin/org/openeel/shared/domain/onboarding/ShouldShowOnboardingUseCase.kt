package world.respect.shared.domain.onboarding

import com.russhwolf.settings.Settings

class ShouldShowOnboardingUseCase(
    private val settings: Settings
) {

    operator fun invoke() : Boolean {
        return settings.getStringOrNull(KEY_ONBOARDING_SHOWN)?.toBoolean() != true
    }

    companion object {

        const val KEY_ONBOARDING_SHOWN = "onboarding_shown"

    }
}