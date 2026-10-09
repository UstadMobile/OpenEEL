package world.respect.shared.domain.account.sharedschooldevice

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.ext.getJson
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiActivityProfileResource.Companion.KEY_SHARED_DEVICE_SELF_SELECT
import world.respect.libutil.ext.normalizeForEndpoint
import world.respect.shared.domain.account.RespectAccountManager

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
