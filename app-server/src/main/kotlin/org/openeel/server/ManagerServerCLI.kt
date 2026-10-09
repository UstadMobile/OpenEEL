package world.respect.server

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.encodeBase64
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import net.sourceforge.argparse4j.inf.Namespace
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.AddSchoolUseCase.Companion.DEFAULT_ADMIN_USERNAME
import world.respect.server.domain.school.demoapp.DemoStringMaps
import world.respect.server.domain.school.demoapp.MakeDemoAppCollectionUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppGradeCollectionsUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitHtmlUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitManifestUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppLearningUnitTinCanXmlUseCase
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase
import world.respect.server.domain.school.demoapp.SaveDemoAppToStaticFilesUseCase
import java.io.File
import java.util.Properties
import kotlin.system.exitProcess
import kotlin.time.Clock

fun managerServerMain(ns: Namespace) {
    val json = Json {
        encodeDefaults = false
        prettyPrint = true
    }
    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(json = json)
        }
    }

    val xml = XML.v1 {
        recommended_1_0_0()
        xmlVersion = XmlVersion.XML10
    }

    val dataDir = ns.getString("datadir")?.let { File(it) }
        ?: File("${ktorAppHomeDir().absolutePath}/$DEFAULT_DATA_DIR_NAME")
    println("DataDir=$dataDir")

    val serverPropertiesFile = ktorServerPropertiesFile(dataDir = dataDir)

    if(!serverPropertiesFile.exists()) {
        println("Error: Server is not running: server.properties does not exist")
        exitProcess(1)
    }

    val serverProperties = Properties()
    serverPropertiesFile.reader().use { serverPropertiesReader ->
        serverProperties.load(serverPropertiesReader)
    }

    val port = serverProperties.getProperty(SERVER_PROPERTIES_KEY_PORT)

    val systemConfigAuth = File(dataDir, DIRECTORY_ADMIN_FILENAME).readText().trim()

    println("Connect to $port using auth $systemConfigAuth")

    val serverUrl = Url("http://localhost:$port/")
    val authHeader = "Basic ${"admin:$systemConfigAuth".encodeBase64()}"

    runBlocking {
        when(ns.getString("subparser_name")) {
            CMD_ADD_SCHOOL -> {
                val schoolBaseUrl = Url(ns.getString("url"))
                val rpId = ns.getString("rpId") ?: schoolBaseUrl.host
                val nsDirUrl: String? = ns.getString("inDirectoryUrl")

                val response = httpClient.post(
                    serverUrl.appendEndpointSegments("api/directory/school")
                ) {
                    header(HttpHeaders.Authorization, authHeader)
                    contentType(ContentType.Application.Json)
                    setBody(
                        listOf(
                            AddSchoolUseCase.AddSchoolRequest(
                                school = SchoolDirectoryEntry(
                                    name = LangMapStringValue(ns.getString("name")),
                                    self = schoolBaseUrl,
                                    xapi = schoolBaseUrl.appendEndpointSegments("api/school/xapi"),
                                    respectExt = schoolBaseUrl.appendEndpointSegments("api/school/respect"),
                                    //Will be set on server
                                    rpId = rpId,
                                    lastModified = Clock.System.now(),
                                    stored = Clock.System.now(),
                                    inDirectoryUrl = nsDirUrl?.let { Url(it) },
                                ),
                                dbUrl = ns.getString("dburl") ?: schoolBaseUrl.sanitizedForFilename(),
                                adminUsername = ns.getString("adminusername") ?: DEFAULT_ADMIN_USERNAME,
                                adminPassword = ns.getString("adminpassword"),
                            )
                        )
                    )
                }
                println("Response: ${response.status}")
            }

            CMD_MAKE_DEMO_APP -> {
                val baseUrl = Url(ns.getString("url"))
                val destDir = File(ns.getString("dir"))
                val demoStrings = DemoStringMaps.initFromResources(json = json)

                val saveDemoAppToStaticFilesUseCase = SaveDemoAppToStaticFilesUseCase(
                    makeDemoAppManifestUseCase = MakeDemoAppManifestUseCase(
                        demoStrings = demoStrings
                    ),
                    makeDemoAppCollectionUseCase = MakeDemoAppCollectionUseCase(
                        demoStringMaps = demoStrings
                    ),
                    makeDemoAppGradeCollectionsUseCase = MakeDemoAppGradeCollectionsUseCase(
                        demoStrings = demoStrings
                    ),
                    makeDemoAppLearningUnitManifestUseCase = MakeDemoAppLearningUnitManifestUseCase(
                        demoStrings = demoStrings
                    ),
                    makeDemoAppLearningUnitTinCanXmlUseCase = MakeDemoAppLearningUnitTinCanXmlUseCase(
                        demoStrings = demoStrings
                    ),
                    makeDemoAppLearningUnitHtmlUseCase = MakeDemoAppLearningUnitHtmlUseCase(
                        demoStrings = demoStrings
                    ),
                    xml = xml,
                    json = json,
                )

                saveDemoAppToStaticFilesUseCase(
                    destDir = destDir,
                    baseUrl = baseUrl,
                )
            }
        }

        exitProcess(0)
    }
}