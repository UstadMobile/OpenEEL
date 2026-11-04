package org.openeel.datalayer.db.school.xapi

import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.quote
import io.ktor.util.sha1
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.libxapi.test.AbstractXapiActivityProfileResourceTest
import org.openeel.datalayer.db.school.insertAdmin
import org.openeel.datalayer.db.school.testSchoolDb
import org.openeel.datalayer.db.school.toDataSource
import org.openeel.lib.dataloadstate.datetime.roundToEpochSeconds
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.time.Clock

class XapiActivityProfileResourceDbTest : AbstractXapiActivityProfileResourceTest() {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()


    override suspend fun withXapiDocumentResource(
        authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
        block: suspend (XapiActivityProfileResource) -> Unit
    ) {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
                authenticatedAgents = authenticatedAgents,
            ).also {
                it.insertAdmin()
            }

            block(dataSource.xapiResource.activityProfile)
        }
    }


    @Test
    fun givenDocument_whenUpdateLocalCalled_thenCanBeRetrieved() = runBlocking {
        testSchoolDb(temporaryFolder.newFolder()) { db ->
            val dataSource = db.toDataSource(
                authenticatedUserUid = "1",
                schoolUrl = Url("http://localhost:8098/"),
            ).also {
                it.insertAdmin()
            }

            val resource = dataSource.xapiResource.activityProfile
            val params = XapiActivityProfileResource.SingleDocumentParams(
                activityId = "http://example.com/activities/course-1",
                profileId = "profile-1",
            )
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
