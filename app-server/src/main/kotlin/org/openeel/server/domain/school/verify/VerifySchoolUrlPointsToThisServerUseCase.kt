package world.respect.server.domain.school.verify

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.http.Url
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withTimeout
import world.respect.server.util.SchoolUrlVerificationManager
import world.respect.server.util.ext.HttpStatusException
import kotlin.time.Duration.Companion.seconds

class VerifySchoolUrlPointsToThisServerUseCase(
    private val verificationManager: SchoolUrlVerificationManager,
    private val httpClient: HttpClient
) {
    suspend operator fun invoke(schoolUrl: Url) = coroutineScope {
        val verificationCode = verificationManager.generateVerificationCode()

        val verificationEndpoint = Url(
            "${schoolUrl.toString().removeSuffix("/")}/.well-known/respect-server-verify?code=$verificationCode"
        )

        // Start waiting for the verification code BEFORE making the request
        val verificationResultDeferred = async {
            verificationManager.waitForVerification(verificationCode)
        }

        try {
            withTimeout(10.seconds) {
                val response = httpClient.get(verificationEndpoint)
                if (response.status != HttpStatusCode.OK) {
                    throw HttpStatusException(
                        "Could not verify '$schoolUrl'. The URL does not appear to point to server instance. ${response.status}",
                        HttpStatusCode.BadRequest
                    )
                }
            }
        } catch (e: Exception) {
            throw HttpStatusException(
                "Could not connect to '$schoolUrl'. Please ensure the URL is accessible and points to server.",
                HttpStatusCode.BadRequest,
                e
            )
        }

        val codeConfirmed = verificationResultDeferred.await()

        if (!codeConfirmed) {
            throw HttpStatusException(
                "Could not verify '$schoolUrl'. The URL does not appear to point to server instance.",
                HttpStatusCode.BadRequest
            )
        }
    }
}
