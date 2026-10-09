package world.respect.shared.domain.account

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.AuthToken

/**
 * Internal authorization response.
 */
@Serializable
class AuthResponse(
    val token: AuthToken,
    val person: Person,
)
