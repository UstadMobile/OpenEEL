package world.respect.credentials.passkey

import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.request.GetPasskeyProviderInfoUseCase

interface CreatePasskeyUseCase {

    sealed class CreatePasskeyResult

    data class PasskeyCreatedResult(
        val authenticationResponseJSON : AuthenticationResponseJSON,
        val respectUserHandle: RespectUserHandle,
        val passkeyProviderInfo: GetPasskeyProviderInfoUseCase.PasskeyProviderInfo
    ) : CreatePasskeyResult()

    class UserCanceledResult : CreatePasskeyResult(){
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is GetCredentialUseCase.UserCanceledResult) return false
            return true
        }

        override fun hashCode(): Int {
            return this::class.hashCode()
        }
    }

    data class Error(
        val message: String?
    ) : CreatePasskeyResult()


    data class Request(
        val personUid: String,
        val username: String,
        val rpId: String,
    )

    suspend operator fun invoke(request: Request): CreatePasskeyResult

}