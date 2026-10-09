package org.openeel.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.openeel.datalayer.db.school.entities.PullSyncStatusEntity

@Dao
interface PullSyncStatusEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(statuses: List<PullSyncStatusEntity>)

    @Query("""
        SELECT PullSyncStatusEntity.*
          FROM PullSyncStatusEntity
         WHERE PullSyncStatusEntity.pssAccountPersonUidNum = :personUidNum
           AND PullSyncStatusEntity.pssTableId = :tableId
    """)
    suspend fun getStatus(
        personUidNum: Long,
        tableId: Int,
    ): PullSyncStatusEntity?

}