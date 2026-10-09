package org.openeel.shared.domain.account.sharedschooldevice.setpin

import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.ext.getJson
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiActivityProfileResource.Companion.KEY_SHARED_DEVICE_PIN
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.shared.domain.account.RespectAccountManager
import kotlin.random.Random

interface GetSharedDevicePINUseCase {
    suspend operator fun invoke(): String
}

class GetSharedDevicePINUseCaseImpl(
    private val schoolDataSource: SchoolDataSource,
    private val respectAccountManager: RespectAccountManager,
    private val json: Json,
    private val setSharedDevicePINUseCase: SetSharedDevicePINUseCase,
) : GetSharedDevicePINUseCase {

    override suspend fun invoke(): String {
        val schoolUrl =
            respectAccountManager.activeAccount?.school?.self?.normalizeForEndpoint()?.toString()
                ?: throw IllegalStateException("No active school")

        val existingPin = schoolDataSource.xapiResource.activityProfile.getJson(
            docParams = XapiActivityProfileResource.SingleDocumentParams(
                activityId = schoolUrl,
                profileId = KEY_SHARED_DEVICE_PIN,
            ),
            json = json,
            deserializer = String.serializer(),
        ).dataOrNull()

        return if (existingPin != null) {
            existingPin
        } else {
            val newPin = generateRandomPin()
            setSharedDevicePINUseCase(newPin)
            newPin
        }
    }

    private fun generateRandomPin(): String {
        return Random.nextInt(1000, 10000).toString().padStart(4, '0')
    }
}
