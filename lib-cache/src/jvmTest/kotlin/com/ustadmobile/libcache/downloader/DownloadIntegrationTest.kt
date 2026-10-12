package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import com.ustadmobile.libcache.okhttp.AbstractCacheInterceptorTest
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.Url
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.autohead.AutoHeadResponse
import io.ktor.server.plugins.conditionalheaders.ConditionalHeaders
import io.ktor.server.routing.routing
import kotlinx.coroutines.runBlocking
import nl.adaptivity.xmlutil.core.XmlVersion
import nl.adaptivity.xmlutil.serialization.XML
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.openeel.lib.opds.model.Publication
import org.openeel.libutil.ext.resolve
import org.openeel.libutil.findFreePort
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class DownloadIntegrationTest : AbstractCacheInterceptorTest() {

    data class DownloadIntegrationTestContext(
        val port: Int,
        val baseUrl: Url,
    )

    private val xml = XML.v1 {
        recommended_1_0_0()
        xmlVersion = XmlVersion.XML10
    }

    private suspend fun downloadIntegrationTest(
        block: suspend DownloadIntegrationTestContext.() -> Unit
    ) {
        val port = findFreePort()

        val server = embeddedServer(Netty, port = port) {
            install(ConditionalHeaders)
            install(AutoHeadResponse)

            routing {
                staticResources("/resources", "publication")
            }
        }

        println("Port  = $port")
        server.start(wait = false)

        try {
            block(
                DownloadIntegrationTestContext(
                    port = port,
                    baseUrl = Url("http://localhost:$port/resources/"),
                )
            )
        }finally {
            server.stop()
        }
    }

    @Test
    fun givenValidManifestUrl_whenPreparedAndDownloaded_thenShouldDownload() {
        runBlocking {
            downloadIntegrationTest {
                val enqueueUseCase = EnqueuePinPublicationPrepareUseCaseJvm(
                    cacheDb, XXStringHasherCommonJvm()
                )
                val runDownloadUseCase = RunDownloadJobUseCaseImpl(
                    okHttpClient = okHttpClient,
                    db = cacheDb,
                    httpCache = ustadCache,
                )

                val mockEnqueueRunDownloadUseCase: EnqueueRunDownloadJobUseCase = mock {

                }

                val prepareUseCase = PinPublicationPrepareUseCase(
                    db = cacheDb,
                    httpClient = httpClient,
                    cache = ustadCache,
                    enqueueRunDownloadJobUseCase = mockEnqueueRunDownloadUseCase,
                    xml = xml,
                )

                val manifestUrl = baseUrl.resolve("lesson001/lesson001.json")
                val publication: Publication = httpClient.get(manifestUrl).body()

                val downloadJob = enqueueUseCase(manifestUrl)
                prepareUseCase(downloadJob.djUid)
                verifyBlocking(mockEnqueueRunDownloadUseCase) {
                    invoke(downloadJob.djUid)
                }

                runDownloadUseCase(downloadJob.djUid)
                publication.resources?.forEach { link ->
                    val resourceUrl = manifestUrl.resolve(link.href)
                    val cacheEntry = ustadCache.getCacheEntry(resourceUrl.toString())
                    assertNotNull(cacheEntry)

                    //May need to check lock is linked to publication
                    val locks = ustadCache.getLocks(resourceUrl.toString())
                    assertEquals(1, locks.size)
                }

                val downloadJobFinished = cacheDb.downloadJobDao.findByUid(downloadJob.djUid)
                val downloadJobItems = cacheDb.downloadJobItemDao.findAllByJobUid(downloadJob.djUid)
                assertEquals(TransferJobItemStatus.STATUS_COMPLETE_INT,
                    downloadJobFinished!!.djStatus,
                    "Download job status should be complete")
                assertTrue(
                    downloadJobItems.all { item ->
                        item.djiStatus == TransferJobItemStatus.STATUS_COMPLETE_INT
                    },
                    message = "All download job items status should be complete"
                )
            }
        }
    }

}