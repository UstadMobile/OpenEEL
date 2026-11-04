package org.openeel.shared.domain.account.gettokenanduser

import org.openeel.credentials.passkey.RespectCredential
import org.openeel.datalayer.school.model.DeviceInfo
import org.openeel.shared.domain.account.AuthResponse

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