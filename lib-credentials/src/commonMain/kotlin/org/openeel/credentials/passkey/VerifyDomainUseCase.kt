package org.openeel.credentials.passkey

interface VerifyDomainUseCase {
    suspend operator fun invoke(rpId: String): Boolean
}