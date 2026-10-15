package org.openeel.datalayer.school.model

import kotlinx.serialization.Serializable

const val SHARED_DEVICE_SETTINGS_ACTIVITY_ID =
    "https://id.openeel.org/activity/shared-device-settings"
const val SHARED_DEVICE_SETTINGS_PROFILE_ID = "settings"

@Serializable
data class SharedDeviceSettings(
    val pin: String = "",
    val canSelectName: Boolean = true,
)
