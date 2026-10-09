package world.respect.server.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withTimeoutOrNull
import world.respect.libutil.ext.randomString
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * Manages verification codes for school URL validation.
 *
 * This system verifies that a school URL actually points to the same server instance
 * by generating a random code, sending it to the school URL's verification endpoint,
 * and checking if the same code is received back via an in-memory channel.
 *
 * Flow:
 * 1. Generate random verification code
 * 2. Make HTTP request to {school-url}/.well-known/respect-server-verify?code={random-code}
 * 3. Server's verification endpoint receives request and puts code on verification flow
 * 4. AddSchoolUseCase checks if the same code appears in the flow
 * 5. If code matches → URL points to this server ✓
 * 6. If code doesn't match → URL points to different server ✗
 */
class SchoolUrlVerificationManager(
    private val verificationTimeout: Duration = 10.seconds
) {
    private val _verificationFlow = MutableSharedFlow<String>(extraBufferCapacity = 100)
    val verificationFlow: Flow<String> = _verificationFlow.asSharedFlow()

    /**
     * Generate a random verification code
     */
    fun generateVerificationCode(): String {
       return randomString(8)
    }

    /**
     * Called by the verification endpoint when it receives a verification request.
     * This proves the request was received by this server instance.
     */
    suspend fun onVerificationReceived(code: String) {
        _verificationFlow.emit(code)
    }

    /**
     * Wait for a specific verification code to be received within the timeout period.
     * This is called by AddSchoolUseCase to verify the school URL points to this server.
     */
    suspend fun waitForVerification(code: String): Boolean {
        val result = withTimeoutOrNull(verificationTimeout) {
            verificationFlow
                .filter { it == code }
                .firstOrNull()
        }

        return result == code
    }
}
