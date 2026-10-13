package org.openeel.shared.domain.account.sharedschooldevice.setpin

import org.openeel.shared.domain.account.sharedschooldevice.SharedDeviceSettingsStore

interface SetSharedDevicePINUseCase {
    suspend operator fun invoke(pin: String)
}

class SetSharedDevicePINUseCaseImpl(
    private val settingsStore: SharedDeviceSettingsStore,
) : SetSharedDevicePINUseCase {

    override suspend fun invoke(pin: String) {
        settingsStore.put(settingsStore.get().copy(pin = pin))
    }
}
