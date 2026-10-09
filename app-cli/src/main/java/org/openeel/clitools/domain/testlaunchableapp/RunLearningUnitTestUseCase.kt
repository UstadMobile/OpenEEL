package world.respect.clitools.domain.testlaunchableapp

import io.ktor.http.quote
import world.respect.clitools.ext.copyResourceToFile
import world.respect.clitools.ext.mkDirsIfNotExists
import world.respect.clitools.util.SysPathUtil
import world.respect.lib.opds.model.Publication
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import world.respect.shared.domain.validator.ValidatorMessage
import java.io.File
import kotlin.random.Random

/**
 *
 */
@Suppress("BlockingMethodInNonBlockingContext")
class RunLearningUnitTestUseCase {

    data class RunLearningUnitTestParams(
        val publication: Publication,
        val clickSteps: List<SelectRandomPublicationUseCase.ClickStep>,
        val baseDir: File,
        val launchableAppName: String,
        val launcherPackageId: String = APP_PACKAGE_ID,
        val launchableAppPackageId: String?,
        val deviceId: String?,
        val testRequest: TestLaunchableAppUseCase.Request,
        val offline: Boolean = Random.nextBoolean(),
    )

    data class Result(
        val messages: List<ValidatorMessage>
    )

    suspend operator fun invoke(
        params: RunLearningUnitTestParams
    ): Result {
        val outputDir = File(params.baseDir, "output").mkDirsIfNotExists()

        val subFlowDir = params.baseDir.resolve("subflows").mkDirsIfNotExists()

        val mainFlowFile = File(params.baseDir, "test_launchable_app_main.yaml").also {
            this::class.java.copyResourceToFile(
                "/flows/test_launchable_app_main.yaml",it
            )
        }

        listOf("gotoapp.yaml", "download_and_enable_airplanemode.yaml").forEach { resName ->
            this::class.java.copyResourceToFile(
                "/flows/subflows/$resName",
                File(subFlowDir, resName)
            )
        }

        File(params.baseDir, "generated/gotolearningunit.yaml").also {
            if(!it.parentFile.exists())
                it.parentFile.mkdirs()
        }.writeText(
            buildString {
                append("appId: world.respect.app\n")
                append("---\n")
                params.clickSteps.forEach { step ->
                    append("- scrollUntilVisible:\n")
                    append("    timeout: 60000\n")
                    append("    element: ${step.text.quote()}\n")
                    append("- tapOn: ${step.text.quote()}\n")
                }
            }
        )

        File(params.baseDir, "generated/clear_native_app.yaml").writeText(
            buildString {
                append("appId: ${params.launchableAppPackageId ?: "not.applicable" }\n")
                append("---\n")
                append("- clearState: ${params.launchableAppPackageId ?: "not.applicable" }\n")
            }
        )

        val cmd = listOfNotNull(
            SysPathUtil.findCommandInPath("maestro")?.absolutePath
                ?: throw IllegalStateException("No Maestro command found"),
            "test",
            params.deviceId?.let { "--device=$it" },
            "--format=junit",
            "--test-output-dir=${outputDir.absolutePath}",
            "--env=SCHOOL_URL=${params.testRequest.serverUrl}",
            "--env=SCHOOL_ADMIN_USER=${params.testRequest.username}",
            "--env=SCHOOL_ADMIN_PASSWORD=${params.testRequest.password}",
            "--env=TEST_APP_URL=${params.testRequest.manifestUrl}",
            "--env=TEST_APP_MODE=${params.testRequest.mode.id}",
            "--env=OFFLINE_STATUS=${params.offline}",
            "--env=TEST_APP_NAME=${params.launchableAppName}",
            mainFlowFile.absolutePath
        )

        print("\nLesson selected to test:")
        println(params.clickSteps.joinToString(separator = " -> ") {it.text })
        println("Learning unit manifest url: ${params.clickSteps.last().link.href}\n")
        println("**Please wait for the lesson to be opened, then complete the lesson.**")
        println("Once you complete the lesson and return to launcher app xAPI statements will be checked\n")

        println("Running maestro command: " + cmd.joinToString(separator = " "))

        val maestroStatus = ProcessBuilder(cmd).start().waitFor()
        println("Maestro test completed: status=$maestroStatus")
        return if(maestroStatus == 0) {
            Result(emptyList())
        }else {
            Result(
                listOf(
                    ValidatorMessage(
                        sourceUri = params.clickSteps.last().link.href,
                        message = "Maestro run failed: status=$maestroStatus"
                    ).also { println(it.toString()) }
                )
            )
        }
    }

    companion object {

        const val APP_PACKAGE_ID = "world.respect.app"
    }
}