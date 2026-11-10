package org.openeel.lib.directory.keycloak.model

import kotlinx.serialization.Serializable

/**
 * https://www.keycloak.org/docs-api/26.8.1/rest-api/index.html#UserRepresentation
 */
@Serializable
data class KeycloakUserRepresentation(
    val id: String? = null,
    val username: String? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val email: String? = null,
    val emailVerified: Boolean? = null,
    val attributes: Map<String, List<String>> = emptyMap(),
    val enabled: Boolean? = null,
    val self: String? = null,
    val origin: String? = null,
    val createdTimestamp: Long? = null,
    val totp: Boolean? = null,
    val federationLink: String? = null,
    val serviceAccountClientId: String? = null,
    val disableableCredentialTypes: Set<String>? = null,
    val requiredActions: List<String>? = null,
    val realmRoles: List<String>? = null,
    val clientRoles: Map<String, List<String>>? = null,
    val notBefore: Int? = null,
    val applicationRoles: Map<String, List<String>>? = null,
    val groups: List<String>? = null,
    val access: Map<String, Boolean>? = null,
)
