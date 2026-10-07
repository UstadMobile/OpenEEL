package org.openeel.datalayer.db.opds

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.openeel.datalayer.db.school.testSchoolDb
import org.openeel.datalayer.db.school.toDataSource
import org.openeel.datalayer.school.opds.ext.withAbsoluteSelfUrl
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.Publication
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class TestOpdsStorage {

    @JvmField
    @Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun givenOpdsData_whenStoredInDbAndRetrieved_thenMatches() {
        val json = Json { encodeDefaults = false }
        val pubUrl = Url("http://localhost:8098/lesson001.json")
        val publication = json.decodeFromString(
            Publication.serializer(),
            this::class.java.getResourceAsStream(
                "/world/respect/datalayer/db/opds/adapters/lesson001.json"
            )!!.bufferedReader().use { it.readText() }
        ).withAbsoluteSelfUrl(pubUrl)

        val adminUid = "1"

        runBlocking {
            testSchoolDb(temporaryFolder.newFolder()) { db ->
                val schoolDs = db.toDataSource(
                    authenticatedUserUid = adminUid,
                    schoolUrl = Url("http://localhost:8098/"),
                )

                schoolDs.opdsPublicationDataSource.updateOpdsPublication(
                    DataReadyState(
                        data = publication,
                        metaInfo = DataLoadMetaInfo(url = pubUrl,)
                    )
                )

                val loadedPublication = schoolDs.opdsPublicationDataSource.getByUrl(
                    url = pubUrl,
                    params = DataLoadParams(),
                ).dataOrNull()

                assertNotNull(loadedPublication)

                assertEquals(publication.metadata.title, loadedPublication.metadata.title)
                assertEquals(publication.metadata.identifier, loadedPublication.metadata.identifier)
                assertEquals(publication.metadata.subject, loadedPublication.metadata.subject)
                assertEquals(publication.links, loadedPublication.links)
                assertEquals(publication.images, loadedPublication.images)
            }
        }


    }

}