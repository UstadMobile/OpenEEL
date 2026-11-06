package org.openeel.shared.domain.account.shareddevice.isshareddevice

import com.russhwolf.settings.Settings
import org.openeel.shared.domain.account.invite.EnableSharedDeviceModeUseCase.Companion.CURRENT_DEVICE_GUID

class CheckIsSharedDeviceUseCase(
    private val settings: Settings,
) {

    data class Result(
        val accountPersonUid: String?,
    )

    operator fun invoke(): Result {
        return Result(settings.getStringOrNull(CURRENT_DEVICE_GUID))
    }

}