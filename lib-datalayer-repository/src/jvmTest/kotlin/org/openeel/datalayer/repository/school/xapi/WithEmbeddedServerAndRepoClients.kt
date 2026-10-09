package world.respect.datalayer.repository.school.xapi

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.routing.Routing
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.writequeue.XapiRemoteWriteQueueDbImpl
import world.respect.datalayer.http.school.xapi.XapiResourceHttpClient
import world.respect.datalayer.repository.util.mkdirsIfNotExists
import world.respect.datalayer.school.model.AuthToken
import world.respect.lib.test.clientservertest.EmbeddedDataSourceServerContext
import world.respect.lib.test.clientservertest.insertAdminAndDefaultGrants
import world.respect.lib.test.clientservertest.newLocalSchoolDatabase
import world.respect.lib.test.clientservertest.withEmbeddedDataSourceServer
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.remotewritequeue.DrainXapiRemoteWriteQueueUseCase
import world.respect.lib.xapi.remotewritequeue.EnqueueDrainXapiRemoteWriteQueueUseCase
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.local.XapiResourceLocal
import world.respect.libutil.util.time.systemTimeInMillis
import java.io.File
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation as ContentNegotiationClient

class RepositoryTestClient(
    val dir: File,
    val schoolDb: RespectSchoolDatabase,
    val localDataSource: XapiResourceLocal,
    val remoteDataSource: XapiResource,
    val authenticatedUser: AuthenticatedUserPrincipalId,
    private val json: Json = Json,
) {

    private val drainRemoteWriteQueueChannel = Channel<Boolean>(capacity = Channel.UNLIMITED)

    val enqueueDrainRemoteWriteQueueUseCase: EnqueueDrainXapiRemoteWriteQueueUseCase = {
        drainRemoteWriteQueueChannel.send(true)
    }

    val xapiRemoteWriteQueue = XapiRemoteWriteQueueDbImpl(
        schoolDb = schoolDb,
        account = authenticatedUser,
        enqueueDrainRemoteWriteQueueUseCase = enqueueDrainRemoteWriteQueueUseCase,
    )

    val drainRemoteWriteQueueUseCase = DrainXapiRemoteWriteQueueUseCase(
        remoteDataSource = remoteDataSource,
        localDataSource = localDataSource,
        xapiRemoteWriteQueue = xapiRemoteWriteQueue,
        json = json,
    )

    val datasource = XapiResourceRepository(
        local = localDataSource,
        remote = remoteDataSource,
        remoteWriteQueue = xapiRemoteWriteQueue,
        json = json,
    )

    private val clientScope = CoroutineScope(Dispatchers.Default + Job())

    init {
        clientScope.launch {
            while(true) {
                drainRemoteWriteQueueChannel.receive()
                drainRemoteWriteQueueUseCase()
            }
        }
    }

    fun close() {
        clientScope.cancel()
    }

}

data class RepositoryTestContext(
    val serverContext: EmbeddedDataSourceServerContext,
    val clients: List<RepositoryTestClient>,
)

suspend fun withEmbeddedServerAndRepositoryClients(
    workDir: File,
    start: Boolean = true,
    routingConfig: Routing.(EmbeddedDataSourceServerContext) -> Unit,
    numClients: Int = 1,
    localAuthenticatedUser: AuthenticatedUserPrincipalId = AuthenticatedUserPrincipalId("1"),
    getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase,
    json: Json = Json,
    httpClient: HttpClient = HttpClient(OkHttp) {
        install(ContentNegotiationClient) {
            json(json = json)
        }
    },
    block: suspend RepositoryTestContext.() -> Unit
) {
    withEmbeddedDataSourceServer(
        dbDir = File(workDir, "server").mkdirsIfNotExists(),
        getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
        routingConfig = routingConfig,
        start = start,
    ) {
        val schoolUrlVal = this.schoolUrl
        val serverContext = this

        val clients = (1 .. numClients).map { index ->
            val clientDir = File(workDir, "client-$index").mkdirsIfNotExists()
            val (schoolDb, schoolLocalDs) = newLocalSchoolDatabase(
                dir = clientDir,
                schoolUrl = schoolUrlVal,
                localAuthenticatedUser = localAuthenticatedUser,
                getAuthenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
            ).also {
                it.second.insertAdminAndDefaultGrants(
                    schoolDb = it.first,
                )
            }

            RepositoryTestClient(
                dir = clientDir,
                schoolDb = schoolDb,
                localDataSource = schoolLocalDs.xapiResource,
                remoteDataSource = XapiResourceHttpClient(
                    xapiUrl = { schoolUrlVal },
                    httpClient = httpClient,
                    tokenProvider = {
                        AuthToken("secret", systemTimeInMillis(), 3600)
                    },
                    json = json,
                ),
                authenticatedUser = localAuthenticatedUser,
                json = json,
            )
        }

        try {
            block(
                RepositoryTestContext(
                    serverContext = serverContext,
                    clients = clients,
                )
            )
        }finally {
            clients.forEach { it.close() }
        }
    }

}