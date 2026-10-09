package world.respect.credentials.passkey

interface VerifyDomainUseCase {
    suspend operator fun invoke(rpId: String): Boolean
}