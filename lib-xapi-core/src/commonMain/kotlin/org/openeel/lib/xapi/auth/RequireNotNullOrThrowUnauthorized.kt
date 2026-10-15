package org.openeel.lib.xapi.auth

import io.ktor.http.HttpStatusCode
import org.openeel.lib.xapi.exceptions.XapiException

fun <T: Any> requireNotNullOrThrowXapiException(
    value: T?,
    message: String = "Unauthorized"
): T {
    return value ?: throw XapiException(HttpStatusCode.Unauthorized.value, message)
}
