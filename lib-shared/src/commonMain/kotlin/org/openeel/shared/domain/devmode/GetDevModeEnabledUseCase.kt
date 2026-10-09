package world.respect.shared.domain.devmode

import com.russhwolf.settings.Settings

class GetDevModeEnabledUseCase(
    private val settings: Settings
) {

    operator fun invoke(): Boolean {
        return settings.getStringOrNull(SetDevModeEnabledUseCase.KEY_DEV_MODE)?.toBoolean() ?: false
    }

}