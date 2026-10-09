package world.respect.datalayer.exceptions

import world.respect.lib.dataloadstate.throwable.ExceptionWithHttpStatusCode

/**
 * As per http status 401: valid credentials not supplied. This will only happen in the datalayer
 * if a person for the authenticated user is not found in the database.
 */
class UnauthorizedException(
    message: String? = null, cause: Throwable? = null
): IllegalStateException(message, cause), ExceptionWithHttpStatusCode {

    override val statusCode: Int = 401
}