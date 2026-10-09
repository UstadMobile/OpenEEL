package org.openeel.shared.domain.account

import kotlinx.serialization.Serializable
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.AuthToken

/**
 * Internal authorization response.
 */
@Serializable
class AuthResponse(
    val token: AuthToken,
    val person: Person,
)
