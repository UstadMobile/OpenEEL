package org.openeel.shared.domain.account.sharedschooldevice

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.ext.putJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource

const val SHARED_DEVICE_SETTINGS_ACTIVITY_ID =
    "https://id.openeel.org/activity/shared-device-settings"
const val SHARED_DEVICE_SETTINGS_PROFILE_ID = "settings"

@Serializable
data class SharedDeviceSettings(
    val pin: String = "",
    val canSelectName: Boolean = true,
)

class SharedDeviceSettingsStore(
    private val schoolDataSource: SchoolDataSource,
    private val json: Json,
) {
    private val documentParams = XapiActivityProfileResource.SingleDocumentParams(
        activityId = SHARED_DEVICE_SETTINGS_ACTIVITY_ID,
        profileId = SHARED_DEVICE_SETTINGS_PROFILE_ID,
    )

    suspend fun get(): SharedDeviceSettings {
        return when (val result = schoolDataSource.xapiResource.activityProfile.getJson(
            docParams = documentParams,
            json = json,
            deserializer = SharedDeviceSettings.serializer(),
        )) {
            is DataReadyState -> result.data
            is NoDataLoadedState -> when (result.reason) {
                NoDataLoadedState.Reason.NOT_FOUND -> SharedDeviceSettings()
                NoDataLoadedState.Reason.NOT_MODIFIED ->
                    throw IllegalStateException("Shared device settings are not available")
            }
            is DataErrorResult -> throw result.error
            is DataLoadingState -> result.partialData
                ?: throw IllegalStateException("Shared device settings are still loading")
        }
    }

    suspend fun put(settings: SharedDeviceSettings) {
        schoolDataSource.xapiResource.activityProfile.putJson(
            docParams = documentParams,
            document = settings,
            json = json,
            serializer = SharedDeviceSettings.serializer(),
        )
    }
}

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
