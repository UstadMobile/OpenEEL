package world.respect.shared.domain.account.sharedschooldevice

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.xapi.ext.putJson
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiActivityProfileResource.Companion.KEY_SHARED_DEVICE_SELF_SELECT
import world.respect.libutil.ext.normalizeForEndpoint
import world.respect.shared.domain.account.RespectAccountManager

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
