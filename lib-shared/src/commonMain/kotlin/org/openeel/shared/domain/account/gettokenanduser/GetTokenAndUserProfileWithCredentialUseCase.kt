package world.respect.shared.domain.account.gettokenanduser

import world.respect.credentials.passkey.RespectCredential
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.shared.domain.account.AuthResponse

/**
 * Gets a token and user profile given a username and password.
 *
 * Server implementation: creates a token entity in the database (if valid) and then returns the
 * related user profile and token (e.g. for Authorization: Bearer ...).
 *
 * Client implementation: sends username and password to server and receives the token and user
 * profile.
 */
interface GetTokenAndUserProfileWithCredentialUseCase {

    suspend operator fun invoke(
        credential: RespectCredential,
        deviceInfo: DeviceInfo? = null,
    ): AuthResponse

    companion object {

        const val LOGTAG_AUTH = "Auth"

        const val PARAM_NAME_USERNAME = "username"

    }

}