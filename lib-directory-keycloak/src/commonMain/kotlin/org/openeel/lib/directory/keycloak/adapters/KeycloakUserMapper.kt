package org.openeel.lib.directory.keycloak.adapters

import org.openeel.lib.directory.keycloak.model.KeycloakUserRepresentation
import org.openeel.lib.directory.model.Person
import org.openeel.lib.directory.model.PersonGenderEnum
import org.openeel.lib.directory.model.PersonRole

fun KeycloakUserRepresentation.toPerson(roles: List<PersonRole> = emptyList()): Person {
    val keycloakId = requireNotNull(id?.takeIf(String::isNotBlank)) {
        "Keycloak user ID is required to map a Person"
    }

    return Person(
        guid = keycloakId,
        username = username,
        givenName = firstName?.takeIf(String::isNotBlank) ?: username.orEmpty(),
        familyName = lastName.orEmpty(),
        email = email,
        userActive = enabled ?: true,
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = roles,
    )
}
