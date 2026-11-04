package org.openeel.lib.directory.keycloak

import kotlinx.serialization.Serializable

/**
 * https://www.keycloak.org/docs-api/26.8.1/rest-api/index.html#UserRepresentation
 */
@Serializable
data class KeycloakUser(
    val id: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val enabled: Boolean? = null,
    val attributes: Map<String, List<String>> = emptyMap(),
)
