package org.openeel.shared.domain.account.sharedschooldevice

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.xapi.ext.putJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiActivityProfileResource.Companion.KEY_SHARED_DEVICE_SELF_SELECT
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.shared.domain.account.RespectAccountManager

class SetSharedDeviceSelfSelectUseCase(
    private val schoolDataSource: SchoolDataSource,
    private val respectAccountManager: RespectAccountManager,
    private val json: Json,
) {

    suspend operator fun invoke(enabled: Boolean) {
        val schoolUrl = respectAccountManager.activeAccount?.school?.self?.normalizeForEndpoint()?.toString()
            ?: throw IllegalStateException("No active school")

        schoolDataSource.xapiResource.activityProfile.putJson(
            docParams = XapiActivityProfileResource.SingleDocumentParams(
                activityId = schoolUrl,
                profileId = KEY_SHARED_DEVICE_SELF_SELECT,
            ),
            document = enabled.toString(),
            json = json,
            serializer = String.serializer(),
        )
    }
}
