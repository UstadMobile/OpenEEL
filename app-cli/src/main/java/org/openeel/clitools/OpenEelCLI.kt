package world.respect.clitools

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import net.sourceforge.argparse4j.ArgumentParsers
import net.sourceforge.argparse4j.helper.HelpScreenException
import net.sourceforge.argparse4j.inf.ArgumentParserException
import net.sourceforge.argparse4j.inf.Namespace
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import world.respect.datalayer.compatibleapps.model.RespectAppManifest
import world.respect.lib.opds.model.OpdsFeed
import world.respect.lib.opds.model.Publication
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.di.jvmKoinAppModule
import world.respect.shared.domain.validator.ListAndPrintlnValidatorReporter
import world.respect.domain.validator.ValidateLinkUseCase
import world.respect.shared.domain.validator.ValidatorMessage
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppModeEnum
import world.respect.shared.domain.testlaunchableapp.TestLaunchableAppUseCase
import java.io.File
import kotlin.system.exitProcess


@Suppress("unused")
class OpenEelCLI : KoinComponent {

    private val validator: ValidateLinkUseCase by inject()

    private val testLaunchableApp: TestLaunchableAppUseCase by inject()

    fun run(args: Array<String>) {
        val parser = ArgumentParsers.newFor("app-cli").build()
        val subparsers = parser.addSubparsers()
            .title("subcommands")
            .description("valid subcommands")
            .dest("subparser_name")
            .help("additional help")
            .metavar("COMMAND")

        /* 5/Oct: Validator is to be updated, remove from this CLI temporarily
        subparsers.addParser(CMD_VALIDATE).also {
            it.addArgument("-u", "--url")
                .required(true)
                .help("URL to validate")
            it.addArgument("-f", "--nofollow")
                .required(false)
                .setDefault("false")
                .help("Don't follow links")
            it.addArgument("-t", "--type")
                .setDefault("manifest")
                .choices("manifest", "opds-feed", "opds-publication")
                .help("Type of item to validate. Can be a Respect App Manifest, Opds 2.0 Feed, or Opds 2.0 Publication")
            it.addArgument("-o", "--output")
                .choices("error", "warn", "verbose", "debug")
                .setDefault("warn")
                .help("Output verbosity")
            it.addArgument("-i", "--include-respect-opds-checks")
                .choices("true", "false")
                .required(false)
                .setDefault("true")
                .help("Include RESPECT-specific checks on OPDS feeds and publications e.g. to " +
                        "check Manifest can be discovered, lists required resources, etc. Can " +
                        "be set to false to validate only against the OPDS spec, not against " +
                        "the RESPECT requirements.")
        }.help("Validate a RESPECT App Manifest or OPDS Feed of Learning Units")
         */

        subparsers.addParser(CMD_TEST_LAUNCHABLE_APP).also {
            it.addArgument("-m", "--manifest")
                .required(true)
                .help("Launchable app manifest URL")
            it.addArgument("-s", "--serverurl")
                .required(true)
                .help("Server URL to test against")
            it.addArgument("-u", "--username")
                .required(true)
                .help("App username for login")
            it.addArgument("-p", "--password")
                .required(true)
                .help("App password for login")
            it.addArgument("-o", "--outputdir")
                .required(true)
                .help("Output directory for test results")
            it.addArgument("-n", "--mode")
                .choices("webview", "native")
                .required(true)
                .help("Launch mode to expect: Web technology based lesson or native app")
            it.addArgument("--device")
                .help("Device ID to run on e.g. --device emulator-5554")
            it.addArgument("--numunits")
                .help("Number of learning units to test")
                .also { arg ->
                    arg.default = TestLaunchableAppUseCase.DEFAULT_NUM_LEARNING_UNITS.toString()
                }
        }.help("Test a launchable app by selecting learning units at random")

        val ns: Namespace
        try {
            ns = parser.parseArgs(args)
            val subCommand = ns.getString("subparser_name")
            when(subCommand) {
                CMD_VALIDATE -> {
                    val url = ns.getString("url")
                    val minLevel = ValidatorMessage.Level.valueOf(
                        (ns.getString("output") ?: "warn").uppercase()
                    )
                    val noFollow = ns.getString("nofollow")?.ifEmpty { null }
                    val validateType = ns.getString("type")
                    val includeRespectSpecificOpdsChecks = ns.getString("include-respect-opds-checks")
                        ?.ifEmpty { null }

                    val reporter = ListAndPrintlnValidatorReporter(
                        filter = {
                            it.level.ordinal >= minLevel.ordinal
                        }
                    )

                    runBlocking {
                        validator(
                            link = ReadiumLink(
                                href = url,
                                type = when (validateType) {
                                    "manifest" -> RespectAppManifest.MIME_TYPE
                                    "opds-feed" -> OpdsFeed.MEDIA_TYPE
                                    "opds-publication" -> Publication.MEDIA_TYPE
                                    else -> throw IllegalArgumentException("Invalid type: $validateType")
                                },
                            ),
                            options = ValidateLinkUseCase.ValidatorOptions(
                                followLinks = !(noFollow?.toBoolean() ?: false),
                                skipRespectChecks =
                                    !(includeRespectSpecificOpdsChecks?.toBoolean() ?: true),
                            ),
                            refererUrl = url,
                            reporter = reporter,
                            visitedUrls = mutableListOf(),
                        )
                    }

                    val numErrors = reporter.messages.count {
                        it.level == ValidatorMessage.Level.ERROR
                    }
                    println("Errors: $numErrors")

                    if(numErrors > 0) {
                        exitProcess(1)
                    }else {
                        exitProcess(0)
                    }
                }

                CMD_TEST_LAUNCHABLE_APP -> {
                    runBlocking {
                        testLaunchableApp(
                            request = TestLaunchableAppUseCase.Request(
                                manifestUrl = Url(ns.getString("manifest")),
                                serverUrl = Url(ns.getString("serverurl")),
                                username = ns.getString("username"),
                                password = ns.getString("password"),
                                outputDir = File(ns.getString("outputdir")),
                                deviceId = ns.getString("device"),
                                numLearningUnits = ns.getString("numunits").toInt(),
                                mode = TestLaunchableAppModeEnum.forId(ns.getString("mode")),
                            )
                        ).also {
                            exitProcess(status = if(it.messages.isEmpty()) 0 else 1)
                        }
                    }
                }
            }
        }catch(e : ArgumentParserException) {
            parser.handleError(e)
            exitProcess(if (e is HelpScreenException) 0 else 1)
        }
    }

    companion object {

        const val CMD_VALIDATE = "validate"

        const val CMD_TEST_LAUNCHABLE_APP = "test-launchable-app"


        /**
         * DO NOT ATTEMPT TO RUN USING THE PLAY BUTTON IN ANDROID STUDIO! Resources will not be
         * found and it will not work.
         *
         * It can be run using as a Gradle task (respect-cli:run --args='..').
         */
        @JvmStatic
        fun main(args: Array<String>) {
            startKoin {
                modules(jvmKoinAppModule)
                modules(cliKoinAppModule)
            }

            OpenEelCLI().run(args)
        }
    }
}