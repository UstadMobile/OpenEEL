package org.openeel.datalayer.db.school.opds.crawlpublications

import org.openeel.datalayer.db.school.opds.entities.CrawlPublicationQueueItemEntity
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItem

fun CrawlPublicationQueueItem.toEntity(): CrawlPublicationQueueItemEntity {
    return CrawlPublicationQueueItemEntity(
        jobId = jobId,
        itemUrl = itemUrl,
        depth = depth,
        status = status,
    )
}

fun CrawlPublicationQueueItemEntity.toModel(): CrawlPublicationQueueItem {
    return CrawlPublicationQueueItem(
        itemId = itemId,
        jobId = jobId,
        itemUrl = itemUrl,
        depth = depth,
        status = status,
    )
}