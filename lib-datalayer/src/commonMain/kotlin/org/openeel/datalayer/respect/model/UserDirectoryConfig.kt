package org.openeel.datalayer.respect.model

import io.ktor.http.Url
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
sealed interface UserDirectoryConfig {

    val directoryType: UserDirectoryConfigTypeEnum

}

@Serializable
enum class UserDirectoryConfigTypeEnum {

    @SerialName("keycloak")
    KEYCLOAK,

}

/**
 * endpoint is realm = https://respect.com/realms/school1
 */
@Serializable
@SerialName("keycloak")
data class KeycloakUserDirectoryConfig(
    override val directoryType: UserDirectoryConfigTypeEnum = UserDirectoryConfigTypeEnum.KEYCLOAK,
    val endpoint: Url,
) : UserDirectoryConfig
