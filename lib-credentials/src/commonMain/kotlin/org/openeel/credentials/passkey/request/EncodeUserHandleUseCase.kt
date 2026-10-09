package world.respect.credentials.passkey.request

import world.respect.credentials.passkey.RespectUserHandle

/**
 * UseCase that will encode a user handle into a Byte64 encoded string, suitable for passkey
 * creation (e.g. by CreatePublicKeyCredentialCreationOptionsJsonUseCase which in turn used by
 * CreatePasskeyUseCase).
 *
 * The user handle can then be decoded when a passkey is presented for a user to sign-in to
 * authenticate (by VerifySignInWithPasskeyUseCase).
 */
interface EncodeUserHandleUseCase {

    /**
     * @return the user handle, base64 encoded. The W3C spec specifies that the user handle is an
     * opaque byte sequence.
     */
    operator fun invoke(userHandle: RespectUserHandle): String

}