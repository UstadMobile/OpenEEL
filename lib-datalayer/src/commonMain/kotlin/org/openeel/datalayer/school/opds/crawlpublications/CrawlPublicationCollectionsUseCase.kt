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
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.libutil.ext.resolve

/**
 * Crawls a [org.openeel.lib.opds.model.Publication]'s collections. This uses a producer / consumer
 * Channel fan-out as per:
 * https://kotlinlang.org/docs/channels.html#fan-out
 *
 * The producer consumes a flow of all queued jobs : in one transaction the status of each queued
 * job item will be changed to running and it will be sent from the producer function.
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

    //Could search db based on job id or such . Produce itself is asynchronous
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceJobs(
        jobId: Int
    ) = produce {
        //collect a flow from the database that has all pending jobs.
        queue.queuedItemsFlow(jobId).collect { jobsToStart ->
            queue.updateStatus(
                jobsToStart.map { it.itemId },
                CrawlPublicationQueueItemStatus.RUNNING
            )

            jobsToStart.forEach {
                send(it.copy(status = CrawlPublicationQueueItemStatus.RUNNING))
            }
        }

    }

    fun CoroutineScope.launchProcessor(
        processorId: Int,
        channel: ReceiveChannel<CrawlPublicationQueueItem>,
    ) = launch {
        for(item in channel) {
            Napier.d(tag = LOGTAG) { "#${item.jobId}-${item.itemId} Process ${item.itemUrl}" }
            val feedLoadState = opdsFeedDataSource.getByUrl(
                url = item.itemUrl,
                params = DataLoadParams()
            )

            feedLoadState.dataOrNull()?.also { feed ->
                val otherFeedLinks = feed.navigation.orEmpty() + feed.groups.orEmpty().flatMap {
                    it.navigation.orEmpty()
                }

                Napier.d(tag = LOGTAG) { "#${item.jobId}-${item.itemId} Found ${otherFeedLinks.size}" }

                queue.insertItemsIfNotInJob(
                    items = otherFeedLinks.map { link ->
                        CrawlPublicationQueueItem(
                            jobId = item.jobId,
                            itemUrl = item.itemUrl.resolve(link.href),
                        )
                    }
                )
            }

            Napier.d(tag = LOGTAG) { "#${item.jobId}-${item.itemId} Finished ${item.itemUrl}" }
            queue.updateStatus(
                itemIds = listOf(item.itemId), CrawlPublicationQueueItemStatus.DONE
            )
        }

        Napier.d("Processor $processorId done")
    }

    suspend operator fun invoke(
        jobId: Int
    ) {
        coroutineScope {
            val producer = produceJobs(jobId)
            repeat(NUM_PROCESSORS) {
                println("Launch processor $it update job list")
                launchProcessor(it, producer)
            }

            println("Wait for all jobs to be done")
            queue.remainingItemsCount(jobId).first { it == 0 }
            println("All done - cancelling")
            producer.cancel()
        }

        println("All done")

    }

    companion object {

        const val LOGTAG = "CrawlPublicationCollection"

        const val NUM_PROCESSORS = 10

    }

}