package org.openeel.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.db.school.opds.entities.CrawlPublicationQueueItemEntity
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItemStatus

@Dao
interface CrawlPublicationQueueItemEntityDao {

    @Query("""
        SELECT COALESCE(MAX(jobId), 0)
          FROM CrawlPublicationQueueItemEntity
    """)
    suspend fun getLargestJobId(): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(item: CrawlPublicationQueueItemEntity)

    @Query("""
        SELECT *
          FROM CrawlPublicationQueueItemEntity
         WHERE jobId = :jobId
           AND status = :queuedStatus
      ORDER BY itemId
    """)
    fun queuedItemsFlow(
        jobId: Int,
        queuedStatus: CrawlPublicationQueueItemStatus,
    ): Flow<List<CrawlPublicationQueueItemEntity>>

    @Query("""
        UPDATE CrawlPublicationQueueItemEntity
           SET status = :status
         WHERE itemId IN (:itemIds)
    """)
    suspend fun updateStatus(
        itemIds: List<Int>,
        status: CrawlPublicationQueueItemStatus,
    )

    @Query("""
        SELECT COUNT(*)
          FROM CrawlPublicationQueueItemEntity
         WHERE CrawlPublicationQueueItemEntity.jobId= :jobId 
           AND status != :doneStatus
           AND status != :failedStatus
    """)
    fun remainingItemsCountFlow(
        jobId: Int,
        doneStatus: CrawlPublicationQueueItemStatus,
        failedStatus: CrawlPublicationQueueItemStatus,
    ): Flow<Int>
}