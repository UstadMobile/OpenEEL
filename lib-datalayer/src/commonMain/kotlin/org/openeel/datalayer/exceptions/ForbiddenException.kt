package world.respect.datalayer.exceptions

import world.respect.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode

/**
 * As per http 403: who the user is is understood, but they do not have permission to perform the
 * requested action.
 */
class ForbiddenException(
    message: String? = null, cause: Throwable? = null
): IllegalStateException(message, cause), ExceptionWithHttpStatusCode {

    override val statusCode: Int = 403
}