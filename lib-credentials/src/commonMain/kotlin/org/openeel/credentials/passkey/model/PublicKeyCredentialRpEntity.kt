package world.respect.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class PublicKeyCredentialRpEntity(
    val name: String,
    val id: String,
    val icon: String?,
)
