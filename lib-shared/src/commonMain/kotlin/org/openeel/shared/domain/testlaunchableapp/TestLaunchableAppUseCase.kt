package world.respect.shared.domain.testlaunchableapp

import io.ktor.http.Url
import world.respect.shared.domain.validator.ValidatorMessage
import java.io.File

/**
 * This test case will select a defined number of learning units at random from the launchable app's
 * manifest.
 */
fun interface TestLaunchableAppUseCase {

    /**
     * @param manifestUrl the URL of the launchable app's manifest
     * @param serverUrl the URL of the server that will be logged into
     * @param username the username to log into the server with
     * @param password the password to log into the server with
     * @param numLearningUnits the number of learning units to select
     */
    data class Request(
        val manifestUrl: Url,
        val serverUrl: Url,
        val username: String,
        val password: String,
        val outputDir: File,
        val mode: TestLaunchableAppModeEnum,
        val deviceId: String?,
        val numLearningUnits: Int = 2,
    )

    data class Result(
        val messages: List<ValidatorMessage>,
    )

    suspend operator fun invoke(request: Request): Result

    companion object {

        const val DEFAULT_NUM_LEARNING_UNITS = 5
    }

}