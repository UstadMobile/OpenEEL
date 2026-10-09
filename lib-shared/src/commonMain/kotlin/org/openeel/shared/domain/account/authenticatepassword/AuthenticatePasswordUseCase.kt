package world.respect.shared.domain.account.authenticatepassword

import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.school.model.Person

interface AuthenticatePasswordUseCase {

    data class Response(
        val authenticatedPerson: Person
    )

    suspend operator fun invoke(
        credential: RespectPasswordCredential
    ): Response

}