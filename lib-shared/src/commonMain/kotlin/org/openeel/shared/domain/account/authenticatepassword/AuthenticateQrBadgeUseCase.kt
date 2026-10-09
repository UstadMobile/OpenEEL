package world.respect.shared.domain.account.authenticatepassword

import world.respect.credentials.passkey.RespectQRBadgeCredential
import world.respect.datalayer.school.model.Person

interface AuthenticateQrBadgeUseCase {
    data class Response(
        val authenticatedPerson: Person
    )

    suspend operator fun invoke(
        credential: RespectQRBadgeCredential
    ): Response
}