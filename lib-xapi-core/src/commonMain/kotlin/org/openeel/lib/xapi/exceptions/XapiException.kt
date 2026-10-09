package world.respect.lib.xapi.exceptions

import world.respect.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode

class XapiException(
    val httpStatusCode: Int,
    message: String,
    cause: Throwable? = null,
) : Exception(message, cause), ExceptionWithHttpStatusCode {

    override val statusCode: Int
        get() = httpStatusCode
}
