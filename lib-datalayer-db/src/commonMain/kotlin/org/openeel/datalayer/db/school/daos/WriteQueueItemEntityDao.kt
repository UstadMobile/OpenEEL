package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.WriteQueueItemEntity

@Dao
interface WriteQueueItemEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: List<WriteQueueItemEntity>)

    @Query(
        """
        SELECT WriteQueueItemEntity.* 
          FROM WriteQueueItemEntity
         WHERE WriteQueueItemEntity.wqiAccountGuid = :accountGuid
           AND WriteQueueItemEntity.wqiTimeWritten = 0
      ORDER BY WriteQueueItemEntity.wqiTimeQueued ASC
        LIMIT :limit
    """
    )
    suspend fun getPending(
        accountGuid: String,
        limit: Int
    ): List<WriteQueueItemEntity>

    @Query("""
        UPDATE WriteQueueItemEntity
           SET wqiTimeWritten = :timeWritten
         WHERE wqiQueueItemId IN (:ids)
    """)
    suspend fun updateTimeWritten(
        ids: List<Int>,
        timeWritten: Long
    )

    @Query("""
        SELECT COUNT(*) 
          FROM WriteQueueItemEntity
         WHERE WriteQueueItemEntity.wqiAccountGuid = :accountGuid
           AND WriteQueueItemEntity.wqiTimeWritten = 0
    """)
    fun pendingCountAsFlow(accountGuid: String): Flow<Int>


}