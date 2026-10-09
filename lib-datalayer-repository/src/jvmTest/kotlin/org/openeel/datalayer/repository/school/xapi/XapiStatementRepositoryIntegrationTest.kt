package world.respect.datalayer.repository.school.xapi

import app.cash.turbine.test
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.res.xapiSampleStatements
import world.respect.datalayer.http.server.XapiStatementsResourceRoute
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.resources.XapiStatementsResource
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Duration.Companion.seconds
import kotlin.uuid.Uuid

class XapiStatementRepositoryIntegrationTest {


    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    private val json: Json = Json

    private suspend fun withXapiStatementServerAndRepositoryClients(
        block: suspend RepositoryTestContext.() -> Unit
    ) {
        withEmbeddedServerAndRepositoryClients(
            workDir = temporaryFolder.newFolder(),
            start = true,
            getAuthenticatedXapiAgentsUseCase = { emptyList() },
            routingConfig = { serverContext ->
                XapiStatementsResourceRoute(
                    statementResource = {
                        serverContext.datasourceContext.datasource.xapiResource.statements
                    },
                    json = json,
                )
            },
            block = block,
        )
    }

    @Test
    fun givenStatementCreatedOnClient_whenConnected_thenWillBeStoredOnServer() = runBlocking {
        withXapiStatementServerAndRepositoryClients {
            val client = clients.first()
            val stmtUuid = Uuid.random()

            val statement: XapiStatement = xapiSampleStatements().first().let {
                json.decodeFromJsonElement(XapiStatement.serializer(), it.jsonObject)
            }.copy(id = stmtUuid)

            client.datasource.statements.post(listOf(statement))

            serverContext.datasourceContext.datasource.xapiResource.statements.getAsFlow(
                listParams = XapiStatementsResource.GetStatementParams(
                    statementId = stmtUuid,
                ),
                dataLoadParams = DataLoadParams(),
            ).mapNotNull {
                it.dataOrNull()
            }.filter {
                it.statements.firstOrNull()?.id == stmtUuid
            }.test(timeout = 5_000.seconds) {
                val stmtFromServer = awaitItem().statements.firstOrNull()
                assertNotNull(stmtFromServer)
                assertEquals(stmtUuid, stmtFromServer.id)
            }
        }
    }

    @Test
    fun givenStatementOnServer_whenGetOnRepoCalled_thenWillBeFetched() {
        runBlocking {
            withXapiStatementServerAndRepositoryClients {
                val statement: XapiStatement = xapiSampleStatements().first().let {
                    json.decodeFromJsonElement(it.jsonObject)
                }

                val stmtUuid = statement.id!!
                val client = clients.first()

                serverContext.datasourceContext.datasource.xapiResource.statements.post(
                    listOf(statement)
                )

                val stmtFromClient = client.datasource.statements.get(
                    listParams = XapiStatementsResource.GetStatementParams(
                        statementId = stmtUuid,
                    )
                ).dataOrNull()?.statements?.firstOrNull()

                assertNotNull(stmtFromClient)
                assertEquals(stmtUuid, stmtFromClient.id)
            }
        }
    }
}