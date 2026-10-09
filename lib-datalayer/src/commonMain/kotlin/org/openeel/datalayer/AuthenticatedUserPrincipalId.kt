package world.respect.datalayer

import kotlinx.serialization.Serializable

/**
 * Represents an authenticated user principal : the server has checked the authorization header
 * and determined the user is authenticated.
 */
@Serializable
data class AuthenticatedUserPrincipalId(
    val guid: String,
) {

    companion object {

        const val DIRECTORY_ADMIN_GUID = "directory-admin"

        val directoryAdmin = AuthenticatedUserPrincipalId(DIRECTORY_ADMIN_GUID)

    }

}