package org.openeel.shared.domain.account.sharedschooldevice.setpin

import org.openeel.shared.domain.account.sharedschooldevice.GetSharedDeviceSettingsUseCase

interface GetSharedDevicePINUseCase {
    suspend operator fun invoke(): String
}

class GetSharedDevicePINUseCaseImpl(
    private val getSharedDeviceSettingsUseCase: GetSharedDeviceSettingsUseCase,
) : GetSharedDevicePINUseCase {

    override suspend fun invoke(): String {
        return getSharedDeviceSettingsUseCase().pin
    }
}
