package org.openeel.credentials.passkey.request

import org.openeel.credentials.passkey.RespectUserHandle


/**
 * Decode a user handle encoded by EncodeUserHandleUseCase - see RespectUserHandle for details on
 * how this works.
 */
interface DecodeUserHandleUseCase {

    operator fun invoke(encodedHandle: String): RespectUserHandle

}