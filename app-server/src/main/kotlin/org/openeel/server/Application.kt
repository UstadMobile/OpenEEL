package org.openeel.server

import io.github.aakira.napier.Napier
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.bearer
import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import org.koin.ktor.ext.getKoin
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger
import org.openeel.libutil.ext.randomString
import org.openeel.server.routes.AUTH_CONFIG_DIRECTORY_ADMIN_BASIC
import org.openeel.server.routes.AuthRoute
import org.openeel.server.routes.RespectSchoolDirectoryRoute
import org.openeel.server.routes.getRespectSchoolJson
import java.io.File
import java.util.Properties
import org.koin.ktor.ext.inject
import org.openeel.demo.demolaunchableappserver.DemoLaunchableAppManifestRoute
import org.openeel.demo.demolaunchableappserver.DemoLaunchableAppCollectionsRoute
import org.openeel.Greeting
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.RespectAppDataSource
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.http.server.XapiStatementsResourceRoute
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.libutil.ext.RESPECT_SCHOOL_LINK_SEGMENT
import org.openeel.lib.dataloadstate.throwable.unwrapHttpStatusCode
import org.openeel.server.demoapp.DemoLaunchableAppLessonRoute
import org.openeel.server.logging.LogbackAntiLog
import org.openeel.server.routes.passkey.GetAllActivePasskeysRoute
import org.openeel.server.routes.passkey.RevokePasskeyRoute
import org.openeel.server.routes.passkey.VerifySignInWithPasskeyRoute
import org.openeel.server.routes.qrcode.PersonQrBadgeRoute
import org.openeel.server.routes.school.respect.AddChildAccountRoute
import org.openeel.server.routes.school.respect.ClassRoute
import org.openeel.server.routes.school.respect.EnrollmentRoute
import org.openeel.server.routes.school.respect.InviteInfoRoute
import org.openeel.server.routes.school.respect.InviteRoute
import org.openeel.server.routes.school.respect.PersonPasskeyRoute
import org.openeel.server.routes.school.respect.PersonPasswordRoute
import org.openeel.server.routes.school.respect.PersonRoute
import org.openeel.server.routes.school.respect.PlaylistRoute
import org.openeel.server.routes.school.respect.RedeemInviteRoute
import org.openeel.server.routes.school.respect.SchoolRegistrationRoute
import org.openeel.server.routes.school.respect.SchoolLinkRoute
import org.openeel.server.routes.school.respect.SchoolPermissionGrantRoute
import org.openeel.server.routes.school.respect.SchoolValidationRoute
import org.openeel.server.routes.e2etestartifactsroute.ReceiveE2EArtifactUploadRoute
import org.openeel.datalayer.http.server.XapiActivityProfileResourceRoute
import org.openeel.datalayer.http.server.XapiAgentProfileResourceRoute
import org.openeel.datalayer.http.server.XapiStateResourceRoute
import org.openeel.server.routes.username.UsernameSuggestionRoute
import org.openeel.server.routes.username.checkusernameunique.CheckUsernameUniqueRoute
import org.openeel.server.util.ext.getSchoolKoinScope
import org.openeel.server.util.ext.requireAccountScope
import org.openeel.server.util.ext.virtualHost
import org.openeel.shared.domain.account.validateauth.ValidateAuthorizationUseCase
import org.openeel.shared.domain.e2eartifactupload.E2EArtifactUploadUseCase
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId

const val AUTH_CONFIG_SCHOOL = "auth-school-bearer"

@Suppress("unused") // Used via application.conf
fun Application.module() {
    Napier.takeLogarithm()
    Napier.base(LogbackAntiLog())

    val serverProperties = Properties().apply {
        setProperty(SERVER_PROPERTIES_KEY_PORT, environment.config.port.toString())
    }

    val absoluteDataDir = environment.config.absoluteDataDir()
    absoluteDataDir.takeIf { !it.exists() }?.mkdirs()

    Napier.d("Respect-server: init : Data dir=$absoluteDataDir")

    environment.config.filePropertyOrNull(SERVER_CONFIG_PID_FILE)?.also { pidFile ->
        pidFile.parentFile?.takeIf { !it.exists() }?.mkdirs()
        pidFile.writeText(ProcessHandle.current().pid().toString())
    }

    ktorServerPropertiesFile(
        dataDir = absoluteDataDir
    ).writer().use { serverPropWriter ->
        serverProperties.store(serverPropWriter, null)
    }

    val wellKnownDir = File(ktorAppHomeDir(), "well-known")
    val assetLinksFile = File(wellKnownDir, "assetlinks.json")
    val termsFile = File(wellKnownDir, "terms.html")

    val dirAdminFile = File(environment.config.absoluteDataDir(), DIRECTORY_ADMIN_FILENAME)
    dirAdminFile.takeIf { !it.exists() }?.also {
        it.writeText(randomString(DEFAULT_DIR_ADMIN_PASS_LENGTH))
    }

    install(Koin) {
        slf4jLogger()
        modules(serverKoinModule(environment.config))
    }

    val json = getKoin().get<Json>()
    install(ContentNegotiation) {
        json(
            json = json,
            contentType = ContentType.Application.Json
        )
    }


    install(Authentication) {
        basic(AUTH_CONFIG_DIRECTORY_ADMIN_BASIC) {
            realm = "Access realm directory admin"
            validate { credentials ->
                val adminPassword = dirAdminFile.readText().trim()
                if(credentials.password == adminPassword) {
                    UserIdPrincipal(credentials.name)
                }else {
                    null
                }
            }
        }

        /*
         * School authentication
         */
        bearer(AUTH_CONFIG_SCHOOL) {
            realm = "Access school"
            authenticate { tokenCredential ->
                val schoolScopeId = SchoolDirectoryEntryScopeId(request.virtualHost, null)
                val schoolScope = getKoin().getOrCreateScope<SchoolDirectoryEntry>(
                    schoolScopeId.scopeId
                )
                val validateAuthorizationUseCase: ValidateAuthorizationUseCase = schoolScope.get()

                validateAuthorizationUseCase(
                    ValidateAuthorizationUseCase.BearerTokenCredential(tokenCredential.token)
                )?.let {
                    val serverAccountScopeManager: ServerAccountScopeManager = schoolScope.get()

                    //Ensure that the account scope is created and safely linked to the school scope.
                    //See ServerAccountScopeManager doc for more info.
                    serverAccountScopeManager.getOrCreateAccountScope(
                        AuthenticatedUserPrincipalId(it.guid)
                    )

                    UserIdPrincipal(it.guid)
                }
            }
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            cause.printStackTrace()

            val httpStatusCode = cause.unwrapHttpStatusCode()
            if(httpStatusCode != null) {
                val responseText = cause.message
                val httpStatus = HttpStatusCode.fromValue(httpStatusCode)
                if(responseText != null) {
                    call.respondText(text = responseText, status = httpStatus)
                }else {
                    call.respond(httpStatus)
                }
            }else {
                call.respondText(text = "500: $cause", status = HttpStatusCode.InternalServerError)
            }
        }
    }

    //As per https://ktor.io/docs/server-swagger-ui.html#configure-cors
    install(CORS) {
        anyHost()
        allowHeader(HttpHeaders.ContentType)
    }

    routing {
        get("/") {
            call.respondText("Ktor: ${Greeting().greet()}")
        }

        SchoolRegistrationRoute()

        route(".well-known") {
            getRespectSchoolJson("respect-school.json")

            get("assetlinks.json") {
                call.respondFile(assetLinksFile)
            }

            get("terms.html") {
                if(termsFile.exists()) {
                    call.respondFile(termsFile)
                }else {
                    call.response.cacheControl(CacheControl.NoStore(null))

                    call.respondText(
                        contentType = ContentType.Text.Plain,
                        status = HttpStatusCode.NotFound,
                        text = "Terms/conditions not found: the server administrator can set this as per the INSTALL.md by saving terms.html into the well-known directory."
                    )
                }
            }

            SchoolValidationRoute()
        }


        environment.config.filePropertyOrNull(
            propertyName = SERVER_CONFIG_KEY_STATICFILES
        )?.also { staticFilesDir ->
            staticFiles("/static-extra", staticFilesDir)
        }

        staticResources("/static-resources", "http")

        route(RESPECT_SCHOOL_LINK_SEGMENT) {
            SchoolLinkRoute()
        }

        route("demoapp") {
            staticResources(
                remotePath = "static",
                basePackage = "demoapp",
            )

            get("/") {
                call.respondResource("/demoapp/index.html")
            }

            DemoLaunchableAppManifestRoute()
            DemoLaunchableAppCollectionsRoute()
            DemoLaunchableAppLessonRoute()
        }

        route("api") {
            route("passkey"){

                VerifySignInWithPasskeyRoute(
                    useCase =  { it.getSchoolKoinScope().get() }
                )

                GetAllActivePasskeysRoute(
                    useCase =  { it.getSchoolKoinScope().get() }
                )
                RevokePasskeyRoute(
                    useCase =  { it.getSchoolKoinScope().get() }
                )
            }
            route("directory") {
                val respectAppDataSource: RespectAppDataSource by inject()
                RespectSchoolDirectoryRoute(
                    respectAppDataSource = respectAppDataSource,
                    filterByHost = environment.config.schoolDirsUseVirtualHost()
                )
            }

            route("school") {
                route("xapi") {
                    authenticate(AUTH_CONFIG_SCHOOL) {
                        XapiStatementsResourceRoute(
                            json = json,
                            statementResource = { call ->
                                call.requireAccountScope().get<SchoolDataSource>().xapiResource.statements
                            }
                        )
                        route("activities") {
                            XapiActivityProfileResourceRoute(
                                activityProfileResource = { call ->
                                    call.requireAccountScope().get<SchoolDataSource>().xapiResource.activityProfile
                                }
                            )

                            XapiStateResourceRoute(
                                stateResource = { call ->
                                    call.requireAccountScope().get<SchoolDataSource>().xapiResource.state
                                },
                                json = json,
                            )
                        }
                        route("agents") {
                            XapiAgentProfileResourceRoute(
                                agentProfileResource = { call ->
                                    call.requireAccountScope().get<SchoolDataSource>().xapiResource.agentProfile
                                },
                                json = json,
                            )
                        }
                    }
                }

                route("respect") {
                    route("auth") {
                        AuthRoute()
                    }
                    route("invite") {
                        authenticate(AUTH_CONFIG_SCHOOL, optional = true) {
                            RedeemInviteRoute(
                                redeemInviteUseCase = { it.getSchoolKoinScope().get() }
                            )
                        }
                        InviteInfoRoute(
                            getInviteInfoUseCase = { it.getSchoolKoinScope().get() }
                        )
                    }

                    route("username"){
                        UsernameSuggestionRoute(
                            usernameSuggestionUseCase = { it.getSchoolKoinScope().get() }
                        )

                        CheckUsernameUniqueRoute(
                            checkUsernameUniqueUseCase = { it.getSchoolKoinScope().get() }
                        )
                    }



                    authenticate(AUTH_CONFIG_SCHOOL) {
                        SchoolPermissionGrantRoute()
                        PersonRoute()
                        InviteRoute()
                        PersonPasskeyRoute()
                        PersonPasswordRoute()
                        ClassRoute()
                        EnrollmentRoute()
                        PersonQrBadgeRoute()
                        AddChildAccountRoute(
                            addChildAccountUseCase = { it.requireAccountScope().get() }
                        )
                    }

                    authenticate(AUTH_CONFIG_SCHOOL, optional = true) {
                        PlaylistRoute()
                    }
                }
            }

            if (environment.config.e2eArtifactUploadEnabled()) {
                val e2eUploadsDir = File(
                    environment.config.absoluteDataDir(),
                    E2EArtifactUploadUseCase.DEFAULT_UPLOAD_DIR_NAME
                )

                route(E2EArtifactUploadUseCase.ENDPOINT_DIR) {
                    ReceiveE2EArtifactUploadRoute(e2eUploadsDir = e2eUploadsDir)
                }
            }
        }
    }
}
