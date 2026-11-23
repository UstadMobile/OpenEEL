package org.openeel.shared.domain.catalog.downloadimagesforfeed

import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.Url
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.OpdsFeedMetadata
import org.openeel.lib.opds.model.ReadiumLink
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DownloadImagesForOpdsFeedVisitorTest {

    @Test
    fun downloadsAllImages() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val imageHrefs = (1..7).map { "image-$it.png" }
        val client = HttpClient(MockEngine { request ->
            synchronized(requestedUrls) {
                requestedUrls.add(request.url.toString())
            }
            respond(content = "image")
        })

        DownloadImagesForOpdsFeedVisitor(client).onVisit(
            readyFeed(*imageHrefs.toTypedArray())
        )

        assertEquals(
            imageHrefs.map { "https://example.com/$it" }.toSet(),
            synchronized(requestedUrls) { requestedUrls.toSet() },
        )
        client.close()
    }

    @Test
    fun duplicateImageLinksAreDownloadedOnceAndSubsequentVisitsUseCache() = runBlocking {
        val requestCount = AtomicInteger()
        val client = HttpClient(MockEngine {
            requestCount.incrementAndGet()
            respond(content = "image")
        })
        val visitor = DownloadImagesForOpdsFeedVisitor(client)
        val feed = readyFeed("same-image.png", "same-image.png")

        visitor.onVisit(feed)
        visitor.onVisit(feed)

        assertEquals(1, requestCount.get())
        client.close()
    }

    @Test
    fun individualImageFailuresDoNotStopOtherDownloads() = runBlocking {
        val failedRequests = AtomicInteger()
        val successfulRequests = AtomicInteger()
        val client = HttpClient(MockEngine { request ->
            if (request.url.toString().endsWith("failure.png")) {
                failedRequests.incrementAndGet()
                throw IllegalStateException("Image request failed")
            }
            successfulRequests.incrementAndGet()
            respond(content = "image")
        })

        DownloadImagesForOpdsFeedVisitor(client).onVisit(
            readyFeed("first.png", "failure.png", "last.png")
        )

        assertEquals(1, failedRequests.get())
        assertEquals(2, successfulRequests.get())
        client.close()
    }

    @Test
    fun cancellationPropagatesToInFlightImageRequest() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val requestCancelled = CompletableDeferred<Unit>()
        val client = HttpClient(MockEngine {
            requestStarted.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                requestCancelled.complete(Unit)
            }
        })
        val job = launch {
            DownloadImagesForOpdsFeedVisitor(client).onVisit(readyFeed("image.png"))
        }

        withTimeout(5_000) { requestStarted.await() }
        job.cancelAndJoin()

        assertTrue(requestCancelled.isCompleted)
        client.close()
    }

    @Test
    fun nonReadyFeedDoesNotStartDownloads() = runBlocking {
        val requestCount = AtomicInteger()
        val client = HttpClient(MockEngine {
            requestCount.incrementAndGet()
            respond(content = "image")
        })

        DownloadImagesForOpdsFeedVisitor(client).onVisit(
            DataLoadingState(metaInfo = DataLoadMetaInfo(url = Url(FEED_URL)))
        )

        assertEquals(0, requestCount.get())
        client.close()
    }

    private fun readyFeed(vararg imageHrefs: String) = DataReadyState(
        data = OpdsFeed(
            metadata = OpdsFeedMetadata(title = "Test feed"),
            links = emptyList(),
            navigation = imageHrefs.map { href ->
                ReadiumLink(
                    href = "navigation",
                    alternate = listOf(ReadiumLink(href = href, rel = listOf("icon"))),
                )
            },
        ),
        metaInfo = DataLoadMetaInfo(url = Url(FEED_URL)),
    )

    companion object {
        private const val FEED_URL = "https://example.com/catalog.json"
    }
}