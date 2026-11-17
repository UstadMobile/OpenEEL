package org.openeel.datalayer.school.opds.crawlpublications

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verifyBlocking
import org.mockito.kotlin.verifyNoMoreInteractions
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.opds.crawlpublications.CrawlPublicationQueueDbImpl
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.opds.model.LangMapStringValue
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.OpdsFeedMetadata
import org.openeel.lib.opds.model.OpdsGroup
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.ReadiumMetadata
import org.openeel.libutil.ext.resolve
import java.util.concurrent.ConcurrentHashMap
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.seconds

/**
 * Test [CrawlPublicationCollectionsUseCase]. Use a real CrawlPublicationQueueDbImpl for the
 * CrawlPublicationQueue because its easier to use the real one than mocking it.
 *
 * Use a mock [org.openeel.datalayer.school.opds.OpdsFeedDataSource] to simulate different scenarios.
 */
class CrawlPublicationCollectionsUseCaseTest {

    @Rule
    @JvmField
    val temporaryFolder: TemporaryFolder = TemporaryFolder()

    /**
     * Using a Mock [org.openeel.datalayer.school.opds.OpdsFeedDataSource] that simulates a situation
     * where:
     * baseUrl/collection.json returns an OpdsFeed with a list of 10 subfeeds baseUrl/(gradeNum)/index.json
     *
     * baseUrl/(gradeNum)/index.json links to ten feeds (eg subject feeds) baseUrl/(gradeNum)/subject/(subjectNum)/index.json
     *
     * baseUrl/(gradeNum)/subject/(subjectNum)/index.json contains a feed that has ONLY publications
     *
     * Check that each OPDS feed has been loaded.
     */
    @Test
    fun givenPublicationCrawled_whenCompleted_thenAllLinkedFeedsWillBeLoaded() = runBlocking {
        Napier.base(DebugAntilog())
        val numGrades = 10
        val numSubjects = 10
        val baseUrl = Url("https://example.org/catalog/")
        val rootUrl = baseUrl.resolve("collection.json")
        val gradeUrls = (0 until numGrades).map { gradeNum ->
            baseUrl.resolve("$gradeNum/index.json")
        }

        fun feed(
            url: Url,
            title: String,
            navigation: List<ReadiumLink>? = null,
            groups: List<OpdsGroup>? = null,
            publications: List<Publication>? = null,
        ) = OpdsFeed(
            metadata = OpdsFeedMetadata(title = title),
            links = listOf(ReadiumLink(href = url.toString(), rel = listOf("self"))),
            navigation = navigation,
            groups = groups,
            publications = publications,
        )

        val expectedFeeds = linkedMapOf(
            rootUrl to feed(
                url = rootUrl,
                title = "Catalog 0",
                navigation = gradeUrls.mapIndexed { gradeNum, _ ->
                    ReadiumLink(href = "$gradeNum/index.json")
                },
                groups = listOf(
                    OpdsGroup(
                        metadata = OpdsFeedMetadata(title = "Grades 0"),
                        navigation = listOf(ReadiumLink(href = gradeUrls.first().toString())),
                    )
                ),
            ),
        )

        gradeUrls.forEachIndexed { gradeNum, gradeUrl ->
            expectedFeeds[gradeUrl] = feed(
                url = gradeUrl,
                title = "Grade $gradeNum",
                navigation = listOf(ReadiumLink(href = rootUrl.toString())) +
                    (0 until numSubjects).map { subjectNum ->
                        ReadiumLink(href = "subject/$subjectNum/index.json")
                    },
            )

            (0 until numSubjects).forEach { subjectNum ->
                val subjectUrl = gradeUrl.resolve("subject/$subjectNum/index.json")
                expectedFeeds[subjectUrl] = feed(
                    url = subjectUrl,
                    title = "Subject $subjectNum in grade $gradeNum",
                    publications = listOf(
                        Publication(
                            metadata = ReadiumMetadata(
                                title = LangMapStringValue("Publication $subjectNum in grade $gradeNum")
                            ),
                            links = listOf(
                                ReadiumLink(
                                    href = "https://example.org/books/$gradeNum/$subjectNum.epub",
                                    rel = listOf("http://opds-spec.org/acquisition/open-access"),
                                )
                            ),
                        )
                    ),
                )
            }
        }

        val feedDataSource: OpdsFeedDataSource = mock {
            on { getByUrl(any(), any()) } doAnswer { invocation ->
                val url = invocation.getArgument<Url>(0)
                expectedFeeds[url]?.let { DataReadyState(data = it) }
                    ?: NoDataLoadedState.notFound()
            }
        }

        val database = Room.databaseBuilder<SchoolDatabase>(
            temporaryFolder.newFile("school.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build()

        try {
            val queue = CrawlPublicationQueueDbImpl(database)
            val jobId = queue.insertItemsIfNotInJob(
                items = listOf(CrawlPublicationQueueItem(jobId = 0, itemUrl = rootUrl)),
                newJob = true,
            )

            withTimeout(20.seconds) {
                CrawlPublicationCollectionsUseCase(queue, feedDataSource)(jobId)
            }

            expectedFeeds.keys.forEach { url ->
                verifyBlocking(feedDataSource) {
                    getByUrl(eq(url), any())
                }
            }
            verifyNoMoreInteractions(feedDataSource)
        } finally {
            database.close()
        }
    }

    /**
     * The mock returns a feed for every requested URL, each linking to another deeper feed.
     * Crawling should stop after loading the initial feed and its descendants through
     * [CrawlPublicationCollectionsUseCase.MAX_DEPTH].
     */
    @Test
    fun givenCollectionLinksThatExceedDepthCount_whenDepthCountReached_thenNoMoreCrawlingIsDone() = runBlocking {
        Napier.base(DebugAntilog())
        val rootUrl = Url("https://example.org/path")
        val requestedUrls = ConcurrentHashMap.newKeySet<Url>()
        val feedDataSource: OpdsFeedDataSource = mock {
            on { getByUrl(any(), any()) } doAnswer { invocation ->
                val url = invocation.getArgument<Url>(0)
                requestedUrls.add(url)
                DataReadyState(
                    data = OpdsFeed(
                        metadata = OpdsFeedMetadata(title = url.toString()),
                        links = listOf(ReadiumLink(href = url.toString(), rel = listOf("self"))),
                        navigation = listOf(
                            ReadiumLink(href = "${url.toString().trimEnd('/')}/child")
                        ),
                    )
                )
            }
        }

        val expectedUrls = (0..CrawlPublicationCollectionsUseCase.MAX_DEPTH).map { depth ->
            Url(rootUrl.toString() + "/child".repeat(depth))
        }.toSet()

        val database = Room.databaseBuilder<SchoolDatabase>(
            temporaryFolder.newFile("school-depth.db").absolutePath
        ).setDriver(BundledSQLiteDriver()).build()

        try {
            val queue = CrawlPublicationQueueDbImpl(database)
            val jobId = queue.insertItemsIfNotInJob(
                items = listOf(CrawlPublicationQueueItem(jobId = 0, itemUrl = rootUrl)),
                newJob = true,
            )

            withTimeout(20.seconds) {
                CrawlPublicationCollectionsUseCase(queue, feedDataSource)(jobId)
            }

            assertEquals(expected = expectedUrls, actual = requestedUrls)
            expectedUrls.forEach { url ->
                verifyBlocking(feedDataSource) {
                    getByUrl(eq(url), any())
                }
            }
            verifyNoMoreInteractions(feedDataSource)
        } finally {
            database.close()
        }
    }
}