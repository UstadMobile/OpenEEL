package org.openeel.shared.domain.account.sharedschooldevice

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiActivityProfileResource.Companion.KEY_SHARED_DEVICE_SELF_SELECT
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.shared.domain.account.RespectAccountManager

class GetSharedDeviceSelfSelectUseCase(
    private val schoolDataSource: SchoolDataSource,
    private val respectAccountManager: RespectAccountManager,
    private val json: Json,
) {
    suspend operator fun invoke(): Boolean {
        val schoolUrl =
            respectAccountManager.activeAccount?.school?.self?.normalizeForEndpoint()?.toString()
                ?: throw IllegalStateException("No active school")

        val settingResult = schoolDataSource.xapiResource.activityProfile.getJson(
            docParams = XapiActivityProfileResource.SingleDocumentParams(
                activityId = schoolUrl,
                profileId = KEY_SHARED_DEVICE_SELF_SELECT,
            ),
            json = json,
            deserializer = String.serializer(),
        ).dataOrNull()

        return settingResult?.toBoolean() ?: true
    }
}
