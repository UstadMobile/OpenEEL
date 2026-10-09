package world.respect.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class PasskeyVerifyResult(
    val isVerified:Boolean,
    val personUid:Long,
    var firstName: String? = null,
    var lastName: String ? = null,
)