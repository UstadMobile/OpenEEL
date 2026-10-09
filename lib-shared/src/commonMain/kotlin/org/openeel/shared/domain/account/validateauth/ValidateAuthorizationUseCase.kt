package world.respect.shared.domain.account.validateauth

import world.respect.datalayer.AuthenticatedUserPrincipalId

/**
 * Used on the server validate an authorization header
 */
interface ValidateAuthorizationUseCase {

    sealed class AuthorizationCredential

    @Suppress("unused") //Reserved for future use
    data class BasicAuthCredential(
        val username: String,
        val password: String
    ): AuthorizationCredential()


    data class BearerTokenCredential(
        val token: String
    ): AuthorizationCredential()

    /**
     * Validate the authentication credential. If the credential is not valid, throw an exception
     */
    suspend operator fun invoke(credential : AuthorizationCredential): AuthenticatedUserPrincipalId?

}