package org.openeel.shared.domain.account.sharedschooldevice

class SetSharedDeviceSelfSelectUseCase(
    private val settingsStore: SharedDeviceSettingsStore,
) {

    suspend operator fun invoke(enabled: Boolean) {
        settingsStore.put(settingsStore.get().copy(canSelectName = enabled))
    }
}
