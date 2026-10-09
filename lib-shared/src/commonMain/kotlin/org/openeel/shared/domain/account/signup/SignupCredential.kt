package world.respect.shared.domain.account.signup

import world.respect.credentials.passkey.model.AuthenticationResponseJSON

sealed class SignupCredential {
    data class Password(
        val username: String,
        val password: String
    ) : SignupCredential()


    data class Passkey(
        val username: String,
        val authenticationResponseJSON: AuthenticationResponseJSON
    ) : SignupCredential()
}
