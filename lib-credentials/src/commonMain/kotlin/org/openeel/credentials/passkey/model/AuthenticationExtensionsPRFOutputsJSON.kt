package world.respect.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationExtensionsPRFOutputsJSON(
    val enabled: Boolean? = false
)