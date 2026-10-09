package world.respect.server.util.ext

import io.ktor.http.HttpStatusCode

/**
 * Base exception that can carry HTTP status code
 */
open class HttpStatusException(
    message: String,
    val statusCode: HttpStatusCode = HttpStatusCode.InternalServerError,
    cause: Throwable? = null
) : Exception(message, cause)

/**
 * Extension function for any exception to add status code by wrapping it
 */
fun <T : Throwable> T.withStatusCode(statusCode: HttpStatusCode): HttpStatusException {
    return HttpStatusException(this.message ?: "Error", statusCode, this)
}

/**
 * Extension function to get status code from any exception
 */
fun Throwable.getStatusCode(): HttpStatusCode? {
    return when (this) {
        is HttpStatusException -> this.statusCode
        else -> null
    }
}