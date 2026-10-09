package world.respect.credentials.passkey

/**
 * UseCase to check if passkeys are supported for a given RP. Underlying implementations check:
 * a) The OS version supports passkeys
 * b) Passkeys can be used with the specified rpId domain (on Android > 12 this uses
 *    DomainVerificationManager, on prior versions, only a simple manifest check is done).
 */
interface CheckPasskeySupportUseCase {

    suspend operator fun invoke(): Boolean

}