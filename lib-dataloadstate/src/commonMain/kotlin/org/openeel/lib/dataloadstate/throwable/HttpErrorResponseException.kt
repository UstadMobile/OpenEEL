package org.openeel.lib.dataloadstate.throwable

class HttpErrorResponseException(
    override val statusCode: Int,
    override val message: String?
): IllegalStateException(message), ExceptionWithHttpStatusCode
