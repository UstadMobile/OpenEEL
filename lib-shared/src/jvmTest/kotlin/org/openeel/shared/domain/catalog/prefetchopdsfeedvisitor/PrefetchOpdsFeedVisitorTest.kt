package org.openeel.shared.domain.catalog.prefetchopdsfeedvisitor

import com.ustadmobile.libcache.UstadCache
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
import org.mockito.kotlin.argWhere
import org.mockito.kotlin.atLeastOnce
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.openeel.lib.dataloadstate.DataLoadMetaInfo
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.OpdsFeedMetadata
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.libutil.ext.resolve
import java.util.concurrent.atomic.AtomicInteger
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.milliseconds

class PrefetchOpdsFeedVisitorTest {

    private fun UstadCache.verifyRetentionLockAdded(
        urlStr: String
    ) {
        verifyBlocking(this, atLeastOnce()) {
            addRetentionLocks(
                argWhere { locks -> locks.any { it.url ==  urlStr } }
            )
        }
    }

    @Test
    fun downloadsAllImages() = runBlocking {
        val requestedUrls = mutableListOf<String>()
        val imageHrefs = (1..7).map { "image-$it.png" }
        val cache = mock<UstadCache>()
        val client = HttpClient(MockEngine { request ->
            synchronized(requestedUrls) {
                requestedUrls.add(request.url.toString())
            }
            respond(content = "image")
        })

        PrefetchOpdsFeedVisitor(client, cache).onVisit(
            readyFeed(*imageHrefs.toTypedArray())
        )

        imageHrefs.map { "https://example.com/$it" }.forEach { urlStr ->
            assertTrue( urlStr in requestedUrls)
            cache.verifyRetentionLockAdded(urlStr)
        }

        client.close()
    }

    @Test
    fun duplicateImageLinksAreDownloadedOnceAndSubsequentVisitsUseCache() = runBlocking {
        val requestCount = AtomicInteger()
        val client = HttpClient(MockEngine {
            requestCount.incrementAndGet()
            respond(content = "image")
        })
        val cache = mock<UstadCache>()
        val visitor = PrefetchOpdsFeedVisitor(client, cache)
        val feed = readyFeed("same-image.png", "same-image.png")

        visitor.onVisit(feed)
        visitor.onVisit(feed)

        assertEquals(1, requestCount.get())
        cache.verifyRetentionLockAdded(
            feed.metaInfo.requireUrl().resolve("same-image.png").toString()
        )
        client.close()
    }


    @Test
    fun cancellationPropagatesToInFlightImageRequest() = runBlocking {
        val requestStarted = CompletableDeferred<Unit>()
        val requestCancelled = CompletableDeferred<Unit>()
        val cache = mock<UstadCache>()
        val client = HttpClient(MockEngine {
            requestStarted.complete(Unit)
            try {
                awaitCancellation()
            } finally {
                requestCancelled.complete(Unit)
            }
        })
        val job = launch {
            PrefetchOpdsFeedVisitor(client, cache).onVisit(readyFeed("image.png"))
        }

        withTimeout(5_000.milliseconds) { requestStarted.await() }
        job.cancelAndJoin()

        assertTrue(requestCancelled.isCompleted)
        cache.verifyRetentionLockAdded("https://example.com/image.png")
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