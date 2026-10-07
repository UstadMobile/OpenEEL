package org.openeel.shared.domain.account.authenticatepassword

import org.openeel.credentials.passkey.RespectQRBadgeCredential
import org.openeel.datalayer.school.model.Person

interface AuthenticateQrBadgeUseCase {
    data class Response(
        val authenticatedPerson: Person
    )

    suspend operator fun invoke(
        credential: RespectQRBadgeCredential
    ): Response
}