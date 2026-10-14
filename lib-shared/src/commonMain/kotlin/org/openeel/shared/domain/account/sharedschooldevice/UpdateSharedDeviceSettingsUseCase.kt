package org.openeel.shared.domain.account.sharedschooldevice

class UpdateSharedDeviceSettingsUseCase(
    private val settingsStore: SharedDeviceSettingsStore,
) {
    suspend operator fun invoke(transform: (SharedDeviceSettings) -> SharedDeviceSettings) {
        val currentSettings = settingsStore.get()
        val updatedSettings = transform(currentSettings)
        settingsStore.put(updatedSettings)
    }
}
