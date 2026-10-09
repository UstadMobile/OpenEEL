package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable

@Serializable
class AuthToken(
    val accessToken: String,
    val timeCreated: Long,
    val ttl: Int,
)