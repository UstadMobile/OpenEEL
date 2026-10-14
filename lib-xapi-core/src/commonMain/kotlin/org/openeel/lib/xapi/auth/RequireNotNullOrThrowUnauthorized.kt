package org.openeel.lib.xapi.auth

import org.openeel.lib.xapi.exceptions.XapiException

fun <T: Any> requireNotNullOrThrowXapiException(
    value: T?,
    message: String = "Not authorized"
): T {
    return value ?: throw XapiException(400, message)
}
