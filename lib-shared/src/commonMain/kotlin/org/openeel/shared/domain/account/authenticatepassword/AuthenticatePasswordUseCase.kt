package org.openeel.shared.domain.account.authenticatepassword

import org.openeel.credentials.passkey.OpenEelPasswordCredential
import org.openeel.lib.directory.model.Person

interface AuthenticatePasswordUseCase {

    data class Response(
        val authenticatedPerson: Person
    )

    suspend operator fun invoke(
        credential: OpenEelPasswordCredential
    ): Response

}