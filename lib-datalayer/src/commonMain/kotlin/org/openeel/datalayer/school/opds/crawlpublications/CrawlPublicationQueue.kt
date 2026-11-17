package org.openeel.datalayer.school.opds.crawlpublications

import kotlinx.coroutines.flow.Flow

/**
 * A queue of items that need to be crawled for [CrawlPublicationCollectionsUseCase]. When crawling
 * publications each URL that is loaded normally yields new URLs to crawl.
 */
interface CrawlPublicationQueue {

    /**
     * Adds a [CrawlPublicationQueueItem] to the queue _if_ no other item in the job as per
     * [CrawlPublicationQueueItem.jobId] has the same URL (avoid potential infinite loop).
     *
     * @param items [CrawlPublicationQueueItem] item to queue if no item in the crawl job matches
     *        [CrawlPublicationQueueItem.itemUrl].
     * @param newJob if true, then a new job id is generated and returned.
     * @return the job id as per [CrawlPublicationQueueItem.jobId]. If [newJob] was true, then the
     *         value of the newly assigned job id.
     */
    suspend fun insertItemsIfNotInJob(
        items: List<CrawlPublicationQueueItem>,
        newJob: Boolean = false,
    ): Int

    /**
     * Provides a flow of items in the queue with the status [CrawlPublicationQueueItemStatus.QUEUED]
     * @param jobId the jobId as per [CrawlPublicationQueueItem.jobId]
     * @return a flow with a list of [CrawlPublicationQueueItem] that have the status
     * [CrawlPublicationQueueItemStatus.QUEUED]
     */
    suspend fun queuedItemsFlow(
        jobId: Int,
    ): Flow<List<CrawlPublicationQueueItem>>

    /**
     * Update the status of the given itemId
     * @param itemIds list of item ids as per [CrawlPublicationQueueItem.itemId]
     * @param status status to set
     */
    suspend fun updateStatus(
        itemIds: List<Int>,
        status: CrawlPublicationQueueItemStatus,
    )

    /**
     * Returns a flow of the count of items with a non-final status (items that do not have the
     * status [CrawlPublicationQueueItemStatus.DONE] or [CrawlPublicationQueueItemStatus.FAILED]).
     */
    suspend fun remainingItemsCount(jobId: Int): Flow<Int>

}