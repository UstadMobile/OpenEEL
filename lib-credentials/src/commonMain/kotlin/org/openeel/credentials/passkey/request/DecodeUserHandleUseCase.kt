package org.openeel.credentials.passkey.request

import org.openeel.credentials.passkey.OpenEelUserHandle


/**
 * Decode a user handle encoded by EncodeUserHandleUseCase - see [OpenEelUserHandle] for details on
 * how this works.
 */
interface DecodeUserHandleUseCase {

    operator fun invoke(encodedHandle: String): OpenEelUserHandle

}