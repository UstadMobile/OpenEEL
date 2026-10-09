package world.respect.credentials.passkey.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationExtensionsClientOutputsJSON(
    val prf: AuthenticationExtensionsPRFOutputsJSON? = null
)