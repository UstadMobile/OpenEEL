package org.openeel.datalayer.school.opds.crawlpublications

import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class CrawlPublicationCollectionsUseCaseTest {

    @Test
    fun testIt() = runBlocking {
        CrawlPublicationCollectionsUseCase().invoke(Url("http://example.org/base.opds"))
        println("testIt done")
    }
}