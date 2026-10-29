package org.openeel.shared.domain.account.authenticatepassword

import org.openeel.credentials.passkey.OpenEelQRBadgeCredential
import org.openeel.lib.directory.model.Person

interface AuthenticateQrBadgeUseCase {
    data class Response(
        val authenticatedPerson: Person
    )

    suspend operator fun invoke(
        credential: OpenEelQRBadgeCredential
    ): Response
}