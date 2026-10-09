package world.respect.datalayer.respect.model

import kotlinx.serialization.Serializable

@Serializable
data class AuthenticationOption(
    val name: String,
    val provider: AuthOptionConfig,
)