package org.openeel.shared.domain.account.sharedschooldevice

class GetSharedDeviceSelfSelectUseCase(
    private val settingsStore: SharedDeviceSettingsStore,
) {
    suspend operator fun invoke(): Boolean {
        return settingsStore.get().canSelectName
    }
}
