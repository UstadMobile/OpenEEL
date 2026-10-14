package org.openeel.shared.domain.account.sharedschooldevice

class GetSharedDeviceSettingsUseCase(
    private val settingsStore: SharedDeviceSettingsStore,
) {
    suspend operator fun invoke(): SharedDeviceSettings {
        val settings = settingsStore.get()
        if (settings.pin.isNotBlank()) {
            return settings
        }

        val settingsWithPin = settings.copy(
            pin = kotlin.random.Random.nextInt(1000, 10000).toString().padStart(4, '0'),
        )
        settingsStore.put(settingsWithPin)
        return settingsWithPin
    }
}
