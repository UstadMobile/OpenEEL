package world.respect.lib.dataloadstate.throwable

const val HTTP_STATUS_UNWRAP_MAX_DEPTH_COUNT = 20

/**
 * Some exceptions map to a particular http status code This interface makes it easier for http
 * server and client components to handle exceptions e.g. an http server's exception handling can
 * simply catch the exception and then set the status code directly.
 */
interface ExceptionWithHttpStatusCode {

    val statusCode: Int

}

class ExceptionWithHttpStatusCodeWrapper internal constructor(
    cause: Throwable?,
    message: String?,
    override val statusCode: Int
): Exception(message, cause), ExceptionWithHttpStatusCode

fun Throwable.withHttpStatus(statusCode: Int): Exception {
    return ExceptionWithHttpStatusCodeWrapper(this, message, statusCode)
}

fun Throwable.unwrapHttpStatusCode(
    maxDepth: Int = HTTP_STATUS_UNWRAP_MAX_DEPTH_COUNT,
): Int? {
    var throwable: Throwable? = this

    var depthCount: Int = 0
    while (throwable != null && depthCount < maxDepth) {
        if (throwable is ExceptionWithHttpStatusCode) {
            return throwable.statusCode
        }
        throwable = throwable.cause
        depthCount++
    }

    return null
}

