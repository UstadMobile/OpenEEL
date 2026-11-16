package org.openeel.datalayer.db.school.opds.crawlpublications

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueue
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItem
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItemStatus

class CrawlPublicationQueueDbImpl(
    private val schoolDb: SchoolDatabase,
) : CrawlPublicationQueue {

    override suspend fun insertItemIfNotInJob(
        item: CrawlPublicationQueueItem,
        newJob: Boolean,
    ): Int {
        return schoolDb.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val queueDao = schoolDb.getCrawlPublicationQueueItemEntityDao()
                val jobId = if (newJob) {
                    queueDao.getLargestJobId() + 1
                } else {
                    item.jobId
                }

                queueDao.insertOrIgnore(
                    item.copy(jobId = jobId).toEntity()
                )

                jobId
            }
        }
    }

    override suspend fun queuedItemsFlow(jobId: Int): Flow<List<CrawlPublicationQueueItem>> {
        return schoolDb.getCrawlPublicationQueueItemEntityDao().queuedItemsFlow(
            jobId = jobId,
            queuedStatus = CrawlPublicationQueueItemStatus.QUEUED,
        ).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun updateStatus(itemIds: List<Int>, status: CrawlPublicationQueueItemStatus) {
        schoolDb.getCrawlPublicationQueueItemEntityDao().updateStatus(
            itemIds = itemIds,
            status = status,
        )
    }

    override suspend fun remainingItemsCount(jobId: Int): Flow<Int> {
        return schoolDb.getCrawlPublicationQueueItemEntityDao().remainingItemsCountFlow(
            jobId = jobId,
            doneStatus = CrawlPublicationQueueItemStatus.DONE,
            failedStatus = CrawlPublicationQueueItemStatus.FAILED,
        )
    }
}