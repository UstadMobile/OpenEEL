package org.openeel.datalayer.school.opds.crawlpublications

import io.ktor.http.Url

/**
 * @param itemId the id of this specific queueItem (e.g. primary key)
 * @param jobId the CrawlPublication job id
 * @param itemUrl the URL of the publication
 * @param status [CrawlPublicationQueueItemStatus]
 */
data class CrawlPublicationQueueItem(
    val itemId: Int = 0,
    val jobId: Int,
    val itemUrl: Url,
    val status: CrawlPublicationQueueItemStatus = CrawlPublicationQueueItemStatus.QUEUED,
)
