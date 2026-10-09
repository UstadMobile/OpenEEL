package world.respect.shared.domain.devmode

import com.russhwolf.settings.Settings

class SetDevModeEnabledUseCase(
    private val settings: Settings,
) {

    operator fun invoke(
        enabled: Boolean,
    ) {
        settings.putString(KEY_DEV_MODE, enabled.toString())
    }


    companion object {

        const val KEY_DEV_MODE = "devMode"
    }

}