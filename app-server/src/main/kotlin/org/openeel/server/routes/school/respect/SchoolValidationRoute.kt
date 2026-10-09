package world.respect.server.routes.school.respect

import io.ktor.http.HttpStatusCode
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.inject
import world.respect.server.util.SchoolUrlVerificationManager

/**
 * Well-known routes for server verification
 *
 * These endpoints allow external servers to verify they are communicating
 * with the same server instance.
 */
fun Route.SchoolValidationRoute() {
    val verificationManager: SchoolUrlVerificationManager by inject()

    /**
     * GET /.well-known/respect-server-verify?code=<verification_code>
     *
     * This endpoint is called by the server itself to verify that a school URL
     * actually points to this server instance.
     *
     * When a user registers a school URL, the server generates a random code,
     * makes an HTTP request to this endpoint on the school URL, and checks
     * if the same code is received back via the verification manager.
     *
     * Returns:
     * - 200 OK if verification code is received and processed
     * - 400 Bad Request if code parameter is missing
     */
    get("respect-server-verify") {
        val code = call.request.queryParameters["code"]

        if (code == null) {
            call.respond(
                HttpStatusCode.BadRequest,
                mapOf("error" to "Code parameter is required")
            )
            return@get
        }

        verificationManager.onVerificationReceived(code)

        call.respond(
            HttpStatusCode.OK,
            mapOf("status" to "verified", "message" to "Verification code received")
        )
    }
}
