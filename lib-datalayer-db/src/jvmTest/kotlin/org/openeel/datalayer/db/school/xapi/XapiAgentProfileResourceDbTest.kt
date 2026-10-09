package world.respect.datalayer.db.school.xapi

import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.quote
import io.ktor.util.sha1
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiAgentProfileResourceTest
import org.openeel.libxapi.test.XapiAgentProfileTestParams
import world.respect.datalayer.db.school.insertAdmin
import world.respect.datalayer.db.school.testSchoolDb
import world.respect.datalayer.db.school.toDataSource
import world.respect.lib.dataloadstate.datetime.roundToEpochSeconds
import world.respect.lib.dataloadstate.datetime.toGMTDate
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiDocumentByteArrayImpl
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

class XapiAgentProfileResourceDbTest : AbstractXapiAgentProfileResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    override suspend fun withXapiDocumentResource(
        authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
        block: suspend (XapiAgentProfileResource) -> Unit
    ) {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
                authenticatedAgents = authenticatedAgents,
            ).also {
                it.insertAdmin()
            }

            block(dataSource.xapiResource.agentProfile)
        }
    }


    @Test
    fun givenDocument_whenUpdateLocalCalled_thenCanBeRetrieved() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val params = XapiAgentProfileTestParams.SINGLE_DOC_PARAMS1

            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
                authenticatedAgents = { listOf(params.agent) }
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.agentProfile
            val timestamp = Clock.System.now().roundToEpochSeconds()
            val doc = XapiDocumentByteArrayImpl(
                type = "application/json",
                updated = timestamp.toGMTDate(),
                contents = """{"synced": true}""".encodeToByteArray(),
            )

            resource.updateLocal(params, doc)

            val getResult = resource.get(params)
            val retrievedDoc = getResult.dataOrNull()
            assertNotNull(retrievedDoc)
            assertContentEquals(
                expected = doc.contentsAsByteArray(),
                actual = retrievedDoc.contentsAsByteArray()
            )
            assertEquals(doc.type, retrievedDoc.type)
            assertEquals(timestamp.toGMTDate(), retrievedDoc.updated)
            assertEquals(
                expected = sha1(doc.contentsAsByteArray()).toHexString().quote(),
                actual = getResult.metaInfo.headers[HttpHeaders.ETag]
            )
        }
    }
}
