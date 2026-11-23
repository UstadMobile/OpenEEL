package org.openeel.datalayer.school.opds.crawlpublications

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.openeel.datalayer.school.opds.OpdsFeedDataSource
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ext.allNavigationLinks
import org.openeel.libutil.ext.resolve

/**
 * Crawls a [org.openeel.lib.opds.model.Publication]'s collections. This uses a producer / consumer
 * Channel fan-out as per:
 * https://kotlinlang.org/docs/channels.html#fan-out
 *
 * The producer consumes a flow of all queued jobs : in one transaction the status of each queued
 * job item will be changed to running and it will be sent from the producer function to the
 * fanned-out processors.
 *
 * The processor picks a job, loads the feed (e.g. such that it is cached), and then adds new jobs
 * for links found.
 *
 * Each URL found will be crawled once and only once (even if linked to via different paths).
 */
class CrawlPublicationCollectionsUseCase(
    private val queue: CrawlPublicationQueue,
    private val opdsFeedDataSource: OpdsFeedDataSource,
) {

    /**
     * Interface that can be provided to be invoked on each [OpdsFeed] visited e.g. to download
     * icons, validate content, etc.
     */
    fun interface OpdsFeedVisitor {

        /**
         * @param feed The [OpdsFeed] visited
         */
        suspend fun onVisit(feed: DataLoadState<OpdsFeed>)

    }

    //Could search db based on job id or such . Produce itself is asynchronous
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceJobs(
        jobId: Int
    ) = produce {
        //collect a flow from the database that has all pending jobs.
        queue.queuedItemsFlow(jobId).collect { jobsToStart ->
            queue.updateStatus(
                jobsToStart.map { it.itemId },
                CrawlPublicationQueueItemStatusEnum.RUNNING
            )

            jobsToStart.forEach {
                send(it.copy(status = CrawlPublicationQueueItemStatusEnum.RUNNING))
            }
        }

    }

    fun CoroutineScope.launchProcessor(
        processorId: Int,
        channel: ReceiveChannel<CrawlPublicationQueueItem>,
        visitor: OpdsFeedVisitor?,
    ) = launch {
        for(item in channel) {
            Napier.d(tag = LOGTAG) {
                "#${item.jobId}-${item.itemId} Processor #$processorId start ${item.itemUrl}"
            }

            val feedLoadState = opdsFeedDataSource.getByUrl(
                url = item.itemUrl,
                params = DataLoadParams()
            )

            feedLoadState.dataOrNull()?.also { feed ->
                if (item.depth < MAX_DEPTH) {
                    queue.insertItemsIfNotInJob(
                        items = feed.allNavigationLinks().also {
                            Napier.d(tag = LOGTAG) { "#${item.jobId}-${item.itemId} Found ${it.size} feeds" }
                        }.map { link ->
                            CrawlPublicationQueueItem(
                                jobId = item.jobId,
                                itemUrl = item.itemUrl.resolve(link.href),
                                depth = item.depth + 1,
                            )
                        }
                    )
                }else {
                    Napier.d(tag = LOGTAG) {
                        "#${item.jobId}-${item.itemId} : not adding other feeds - " +
                                "max depth ($MAX_DEPTH) reached"
                    }
                }
            }

            visitor?.onVisit(feedLoadState)

            Napier.d(tag = LOGTAG) { "#${item.jobId}-${item.itemId} Finished ${item.itemUrl}" }
            queue.updateStatus(
                itemIds = listOf(item.itemId), CrawlPublicationQueueItemStatusEnum.DONE
            )
        }

        Napier.d(tag = LOGTAG, message = "Processor $processorId done")
    }

    suspend operator fun invoke(
        jobId: Int,
        visitor: OpdsFeedVisitor? = null,
    ) {
        coroutineScope {
            Napier.d(tag = LOGTAG) { "#$jobId - start job"}
            val producer = produceJobs(jobId)
            repeat(NUM_PROCESSORS) {
                launchProcessor(it, producer, visitor)
            }

            Napier.d(tag = LOGTAG) { "#$jobId - waiting for job items to finish"}
            queue.remainingItemsCount(jobId).first { it == 0 }
            Napier.d(tag = LOGTAG) { "#$jobId - no remaining job items, cancelling producer" }
            producer.cancel()
        }

        Napier.d(tag = LOGTAG) { "#$jobId - DONE"}
    }

    companion object {

        const val LOGTAG = "CrawlPublicationCollection"

        const val NUM_PROCESSORS = 10

        const val MAX_DEPTH = 5

    }

}