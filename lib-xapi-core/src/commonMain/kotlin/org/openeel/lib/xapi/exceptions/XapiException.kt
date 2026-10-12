package org.openeel.lib.xapi.exceptions

import org.openeel.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode

class XapiException(
    val httpStatusCode: Int,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause), ExceptionWithHttpStatusCode {

    override val statusCode: Int
        get() = httpStatusCode
}
