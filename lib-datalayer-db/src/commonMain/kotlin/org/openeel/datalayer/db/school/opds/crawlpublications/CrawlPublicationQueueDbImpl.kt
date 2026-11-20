package org.openeel.datalayer.db.school.opds.crawlpublications

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueue
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItem
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItemStatusEnum

class CrawlPublicationQueueDbImpl(
    private val schoolDb: SchoolDatabase,
) : CrawlPublicationQueue {

    override suspend fun insertItemsIfNotInJob(
        items: List<CrawlPublicationQueueItem>,
        newJob: Boolean,
    ): Int {
        return schoolDb.useWriterConnection { connection ->
            connection.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val queueDao = schoolDb.getCrawlPublicationQueueItemEntityDao()
                val newJobId = if (newJob) {
                    queueDao.getLargestJobId() + 1
                } else {
                    0
                }

                queueDao.insertOrIgnore(
                    items = items.let {
                        if(newJob) {
                            items.map { it.copy(jobId = newJobId) }
                        }else {
                            items
                        }
                    }.map {
                        it.toEntity()
                    }
                )

                newJobId
            }
        }
    }

    override suspend fun queuedItemsFlow(jobId: Int): Flow<List<CrawlPublicationQueueItem>> {
        return schoolDb.getCrawlPublicationQueueItemEntityDao().queuedItemsFlow(
            jobId = jobId,
            queuedStatus = CrawlPublicationQueueItemStatusEnum.QUEUED,
        ).map { entities ->
            entities.map { it.toModel() }
        }
    }

    override suspend fun updateStatus(itemIds: List<Int>, status: CrawlPublicationQueueItemStatusEnum) {
        schoolDb.getCrawlPublicationQueueItemEntityDao().updateStatus(
            itemIds = itemIds,
            status = status,
        )
    }

    override fun remainingItemsCount(jobId: Int): Flow<Int> {
        return schoolDb.getCrawlPublicationQueueItemEntityDao().remainingItemsCountFlow(
            jobId = jobId,
            doneStatus = CrawlPublicationQueueItemStatusEnum.DONE,
            failedStatus = CrawlPublicationQueueItemStatusEnum.FAILED,
        )
    }
}