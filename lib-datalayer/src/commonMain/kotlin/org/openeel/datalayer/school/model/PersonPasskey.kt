package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * @property credentialId the credential id as per https://w3c.github.io/webauthn/#credential-id
 */
@Serializable
data class PersonPasskey(
    val personGuid: String,
    override val lastModified: InstantAsISO8601,
    override val stored: InstantAsISO8601,
    val attestationObj: String?,
    val clientDataJson: String?,
    val originString: String?,
    val credentialId: String,
    val challengeString: String?,
    val publicKey: String?,
    val isRevoked: Boolean,
    val deviceName: String,
    val timeCreated: InstantAsISO8601 = Clock.System.now(),
    val aaguid: String?,
    val providerName: String?,
    val iconLight: String?,
    val iconDark: String?
): ModelWithTimes
