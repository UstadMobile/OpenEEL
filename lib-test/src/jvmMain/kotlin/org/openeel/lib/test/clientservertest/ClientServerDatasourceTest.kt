package world.respect.lib.test.clientservertest

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.routing.Routing
import io.ktor.server.routing.routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.db.RespectAppDataSourceDb
import world.respect.datalayer.db.RespectAppDatabase
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.networkvalidation.ExtendedDataSourceValidationHelperImpl
import world.respect.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import world.respect.datalayer.db.school.writequeue.RemoteWriteQueueDbImpl
import world.respect.datalayer.http.SchoolDataSourceHttpClient
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.SchoolDataSourceRepository
import world.respect.datalayer.repository.school.writequeue.DrainRemoteWriteQueueUseCase
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.school.model.AuthToken
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.opds.model.LangMapStringValue
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.findFreePort
import world.respect.libutil.util.time.systemTimeInMillis
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXHasher64FactoryCommonJvm
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File
import kotlin.time.Clock
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationServer


class ClientServerDataSourceTestBuilder internal constructor(
    private val baseDir: File,
    val json: Json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = false
    },
    numClients: Int = 1,
    val stringHasher: XXStringHasher = XXStringHasherCommonJvm(),
    val adminUserId: AuthenticatedUserPrincipalId = AuthenticatedUserPrincipalId("4242"),
    val useDefaultPermissions: Boolean = true,
) {

    private val serverDir = File(baseDir, "server").also { it.mkdirs() }

    val port = findFreePort()

    val schoolUrl = Url("http://localhost:$port/")

    val serverSchoolSourceAndDb = newLocalSchoolDatabase(
        serverDir, stringHasher, adminUserId
    )

    val serverDb: RespectSchoolDatabase
        get() = serverSchoolSourceAndDb.first

    val serverAdminPerson = Person(
        guid = adminUserId.guid,
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = listOf(
            PersonRole(true, PersonRoleEnum.SYSTEM_ADMINISTRATOR)
        ),
    )

    inner class DataSourceTestClient(
        val schoolDb: RespectSchoolDatabase,
        val schoolDataSource: SchoolDataSource,
        val schoolDataSourceLocal: SchoolDataSourceLocal,
        val schoolDataSourceRemote: SchoolDataSource,
        val validationHelper: ExtendedDataSourceValidationHelper,
        val scope: CoroutineScope,
    ) {

        suspend fun insertServerAdminAndDefaultGrants() {
            schoolDataSourceLocal.personDataSource.updateLocal(listOf(serverAdminPerson))
            if(useDefaultPermissions) {
                AddDefaultSchoolPermissionGrantsUseCase(
                    schoolDb = schoolDb,
                    uidNumberMapper = XXHashUidNumberMapper(stringHasher),
                ).invoke()
            }
        }

        fun close() {
            scope.cancel()
        }

    }

    private lateinit var serverRouting: Routing.() -> Unit

    fun newLocalSchoolDatabase(
        dir: File,
        stringHasher: XXStringHasher,
        localAuthenticatedUser: AuthenticatedUserPrincipalId,
    ) = newLocalSchoolDatabase(
        dir = dir,
        stringHasher = stringHasher,
        localAuthenticatedUser = localAuthenticatedUser,
        schoolUrl = schoolUrl,
    )

    val serverSchoolDataSource = serverSchoolSourceAndDb.also { (database, datasource) ->
        runBlocking {
            datasource.personDataSource.updateLocal(listOf(serverAdminPerson))
            if(useDefaultPermissions) {
                AddDefaultSchoolPermissionGrantsUseCase(
                    schoolDb = database,
                    uidNumberMapper = XXHashUidNumberMapper(stringHasher),
                ).invoke()
            }
        }
    }.second

    val schoolDirectoryEntry = SchoolDirectoryEntry(
        name = LangMapStringValue("test school"),
        self = schoolUrl,
        xapi = schoolUrl.appendEndpointSegments("api/school/xapi"),
        respectExt = schoolUrl.appendEndpointSegments("api/school/respect"),
        rpId = schoolUrl.host,
        lastModified = Clock.System.now(),
        stored = Clock.System.now()
    )

    val server = embeddedServer(Netty, port = port) {
        install(ContentNegotiationServer) {
            json(
                json = json,
                contentType = ContentType.Application.Json
            )
        }

        routing {
            serverRouting()
        }
    }

    val okHttpClient = OkHttpClient.Builder().build()

    val httpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
        engine {
            preconfigured = okHttpClient
        }
    }

    val clients = (0 until numClients).map {
        val clientDir = File(baseDir, "client-$it").also { file -> file.mkdirs() }
        val clientAppDb = Room.databaseBuilder<RespectAppDatabase>(
            File(clientDir, "respect-app.db").absolutePath
        ).setDriver(BundledSQLiteDriver())
            .build()

        val clientAppDataSource: RespectAppDataSourceLocal = RespectAppDataSourceDb(
            respectAppDatabase = clientAppDb,
            json = json,
            xxStringHasher = stringHasher
        )

        val (schoolDb, schoolDataSourceLocal) = newLocalSchoolDatabase(
            clientDir, stringHasher, adminUserId
        )



        runBlocking {
            clientAppDataSource.schoolDirectoryEntryDataSource.updateLocal(
                listOf(schoolDirectoryEntry)
            )
        }

        val clientValidationHelper = spy(
            ExtendedDataSourceValidationHelperImpl(
                respectAppDb = clientAppDb,
                xxStringHasher = XXStringHasherCommonJvm(),
                xxHasher64Factory = XXHasher64FactoryCommonJvm(),
            )
        )

        val token = "secret"
        val schoolDataSourceRemote = SchoolDataSourceHttpClient(
            schoolUrl = schoolUrl,
            schoolDirectoryEntryDataSource = clientAppDataSource.schoolDirectoryEntryDataSource,
            httpClient = httpClient,
            tokenProvider =  { AuthToken(token, systemTimeInMillis(), 3600) },
            validationHelper = clientValidationHelper,
            defaultAppCatalogUrl = null,
            json = json,
        )

        val drainQueueSignal = Channel<Boolean>(capacity = Channel.UNLIMITED)

        val enqueueRemoteWorkUseCase = EnqueueDrainRemoteWriteQueueUseCase {
            drainQueueSignal.send(true)
        }

        val remoteWriteQueue = RemoteWriteQueueDbImpl(
            schoolDb = schoolDb,
            account = adminUserId,
            enqueueDrainRemoteWriteQueueUseCase = enqueueRemoteWorkUseCase,
        )

        val clientScope = CoroutineScope(Dispatchers.Default + Job())

        val clientDataSource = SchoolDataSourceRepository(
            local = schoolDataSourceLocal,
            remote = schoolDataSourceRemote ,
            validationHelper = clientValidationHelper,
            remoteWriteQueue = remoteWriteQueue,
            json = json,
            xapiRemoteWriteQueue = mock {  },
        )

        val drainRemoteWriteQueueUseCase = DrainRemoteWriteQueueUseCase(
            remoteWriteQueue = remoteWriteQueue,
            dataSource = clientDataSource,
        )

        clientScope.launch {
            while(true) {
                drainQueueSignal.receive()
                drainRemoteWriteQueueUseCase()
            }
        }

        DataSourceTestClient(
            schoolDb = schoolDb,
            schoolDataSource = clientDataSource,
            schoolDataSourceLocal = schoolDataSourceLocal,
            schoolDataSourceRemote = schoolDataSourceRemote,
            validationHelper = clientValidationHelper,
            scope = clientScope,
        )
    }

    fun serverRouting(
        block: Routing.() -> Unit
    ) {
        serverRouting = block
    }
}

suspend fun clientServerDatasourceTest(
    baseDir: File,
    useDefaultPermissions: Boolean = true,
    block: suspend ClientServerDataSourceTestBuilder.() -> Unit,
) {
    val testBuilder = ClientServerDataSourceTestBuilder(
        baseDir = baseDir,
        useDefaultPermissions = useDefaultPermissions,
    )

    try {
        block(testBuilder)
    }finally {
        testBuilder.server.stop(100, 1000)
        testBuilder.clients.forEach { it.close() }
    }
}