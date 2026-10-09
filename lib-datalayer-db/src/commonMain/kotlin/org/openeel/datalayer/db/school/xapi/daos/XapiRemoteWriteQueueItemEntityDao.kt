package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.entities.XapiRemoteWriteQueueItemEntity

@Dao
interface XapiRemoteWriteQueueItemEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: List<XapiRemoteWriteQueueItemEntity>)

    @Query(
        """
        SELECT XapiRemoteWriteQueueItemEntity.* 
          FROM XapiRemoteWriteQueueItemEntity
         WHERE XapiRemoteWriteQueueItemEntity.xrqAccountGuid = :accountGuid
           AND XapiRemoteWriteQueueItemEntity.xrqTimeWritten = 0
      ORDER BY XapiRemoteWriteQueueItemEntity.xrqTimeQueued ASC
        LIMIT :limit
    """
    )
    suspend fun getPending(
        accountGuid: String,
        limit: Int
    ): List<XapiRemoteWriteQueueItemEntity>

    @Query("""
        UPDATE XapiRemoteWriteQueueItemEntity
           SET xrqTimeWritten = :timeWritten
         WHERE xrqItemId IN (:ids)
    """)
    suspend fun updateTimeWritten(
        ids: List<Int>,
        timeWritten: Long
    )

    @Query("""
        SELECT COUNT(*) 
          FROM XapiRemoteWriteQueueItemEntity
         WHERE XapiRemoteWriteQueueItemEntity.xrqAccountGuid = :accountGuid
           AND XapiRemoteWriteQueueItemEntity.xrqTimeWritten = 0
    """)
    fun pendingCountAsFlow(accountGuid: String): Flow<Int>

}
