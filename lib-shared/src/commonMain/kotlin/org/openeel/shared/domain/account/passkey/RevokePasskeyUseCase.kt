package world.respect.shared.domain.account.passkey


interface RevokePasskeyUseCase {
    suspend operator fun invoke(personGuid: String)

}