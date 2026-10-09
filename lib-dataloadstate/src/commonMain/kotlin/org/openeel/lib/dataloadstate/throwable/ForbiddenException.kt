package world.respect.lib.dataloadstate.throwable

class ForbiddenException(
    message: String? = null, cause: Throwable? = null
): Exception(message, cause), ExceptionWithHttpStatusCode {

    override val statusCode = 403

}