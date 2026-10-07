package org.openeel.clitools.domain.testlaunchableapp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.clitools.util.SysPathUtil
import org.openeel.credentials.passkey.RespectPasswordCredential
import org.openeel.datalayer.http.school.xapi.XapiStatementsResourceHttpClient
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.findCollection
import org.openeel.lib.xapi.ext.objectActivityOrNull
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.model.XapiVerb
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.libutil.ext.resolve
import org.openeel.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCaseClient
import org.openeel.shared.domain.launchapp.GetAndroidPackageIdForLaunchableAppUseCase
import org.openeel.shared.domain.testlaunchableapp.TestLaunchableAppModeEnum
import org.openeel.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import org.openeel.shared.domain.validator.ValidatorMessage
import org.openeel.shared.ext.selectPreferredString
import java.io.File
import kotlin.time.Clock

/**
 * JVM implementation for [TestLaunchableAppUseCase]. This will
 * a) Select the specified number of learning units at random by following the link from the app
 *    manifest to the app's default collection (an OpdsFeed).
 * b) For each selected learning unit:
 *    i) create a new directory that contains:
 *        The test_launchable_app_main.yaml flow and subflows from the resources
 *        A generated Maestro flow file that navigates to the given learning unit
 *   ii) Run the maestro command to run the test using ProcessBuilder
 *   iii) Wait for the process to complete.
 */
class TestLaunchableAppUseCaseImpl(
    private val selectRandomPublicationUseCase: SelectRandomPublicationUseCase,
    private val runLearningUnitTestUseCase: RunLearningUnitTestUseCase,
    private val getXapiStatementsFromLearningUnitTestUseCase: GetXapiStatementsFromLearningUnitTestUseCase,
    private val httpClient: HttpClient,
    private val json: Json,
    private val getAndroidPackageIdForLaunchableAppUseCase: GetAndroidPackageIdForLaunchableAppUseCase,
): TestLaunchableAppUseCase {

    val prettyPrintJson = Json {
        prettyPrint = true
        encodeDefaults = false
    }


    override suspend fun invoke(
        request: TestLaunchableAppUseCase.Request
    ): TestLaunchableAppUseCase.Result {
        val messages = mutableListOf<ValidatorMessage>()
        if(SysPathUtil.findCommandInPath("maestro") == null) {
            return TestLaunchableAppUseCase.Result(
                messages = listOf(
                    ValidatorMessage(
                        sourceUri = "local",
                        message = "Maestro command not found: please install as per " +
                                "https://docs.maestro.dev/maestro-cli/how-to-install-maestro-cli"
                    )
                )
            )
        }

        val manifestPub: Publication = httpClient.get(request.manifestUrl).body()
        val defaultCollectionUrl = manifestPub.findCollection()?.let {
            request.manifestUrl.resolve(it.href)
        } ?: throw IllegalArgumentException("Manifest does not contain a default collection")
        val appName = manifestPub.metadata.title.selectPreferredString(listOf("en"))
        val launchableAppPackageId = if(request.mode == TestLaunchableAppModeEnum.NATIVE) {
            getAndroidPackageIdForLaunchableAppUseCase(manifestPub)
        }else {
            null
        }

        val authResponse = GetTokenAndUserProfileWithCredentialUseCaseClient(
            schoolUrl = request.serverUrl,
            httpClient = httpClient,
            getDeviceInfoUseCase = null,
        ).invoke(
            credential = RespectPasswordCredential(request.username, request.password)
        )

        val statementResource = XapiStatementsResourceHttpClient(
            httpClient = httpClient,
            xapiUrl = {
                request.serverUrl.appendEndpointSegments("api/school/xapi")
            },
            tokenProvider = { authResponse.token },
            json = json
        )


        for(index in 0 until request.numLearningUnits) {
            val testStartTime = Clock.System.now()
            val learningUnitSelection = selectRandomPublicationUseCase(
                request = SelectRandomPublicationUseCase.Request(defaultCollectionUrl)
            )

            val learningUnitOutputDir = File(request.outputDir, "test_$index")

            runLearningUnitTestUseCase(
                params = RunLearningUnitTestUseCase.RunLearningUnitTestParams(
                    publication = learningUnitSelection.publication,
                    clickSteps = learningUnitSelection.clickPath,
                    baseDir = learningUnitOutputDir,
                    launchableAppName = appName,
                    testRequest = request,
                    launchableAppPackageId = launchableAppPackageId,
                    deviceId = null,
                )
            ).also {
                messages.addAll(it.messages)
            }

            val publicationUrl = Url(learningUnitSelection.clickPath.last().link.href)
            val stmtResult = getXapiStatementsFromLearningUnitTestUseCase(
                publicationUrl = publicationUrl,
                testStartTime = testStartTime,
                statementResource = statementResource,
            )

            File(learningUnitOutputDir, "xapi-statements.json").also {
                print("Found ${stmtResult.allStatements.statements.size} xAPI statements: ")
                println("saving to ${it.absolutePath}")
            }.writeText(
                prettyPrintJson.encodeToString(
                    XapiStatementResult.serializer(),
                    stmtResult.allStatements,
                )
            )

            if(
                !stmtResult.allStatements.statements.filter {
                    it.objectActivityOrNull()?.id == stmtResult.activityId
                }.any {
                    it.verb.id == XapiVerb.ID_COMPLETED || it.verb.id == XapiVerb.ID_PASSED
                            || it.verb.id == XapiVerb.ID_FAILED
                }
            ) {
                messages.add(
                    ValidatorMessage(
                        sourceUri = publicationUrl.toString(),
                        message = "No complete, passed, or failed Xapi Statement for expected " +
                                "activity id (${stmtResult.activityId}) after testing $publicationUrl"
                    ).also {
                        println(it.toString())
                    }
                )
            }
        }

        return TestLaunchableAppUseCase.Result(
            messages = messages.toList()
        )
    }
}