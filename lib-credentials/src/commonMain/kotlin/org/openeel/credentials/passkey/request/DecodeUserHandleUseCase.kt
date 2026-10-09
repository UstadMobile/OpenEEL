package world.respect.credentials.passkey.request

import world.respect.credentials.passkey.RespectUserHandle


/**
 * Decode a user handle encoded by EncodeUserHandleUseCase - see RespectUserHandle for details on
 * how this works.
 */
interface DecodeUserHandleUseCase {

    operator fun invoke(encodedHandle: String): RespectUserHandle

}