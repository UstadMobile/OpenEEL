package org.openeel.lib.xapi.p2psync.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.xapi.p2psync.db.entities.XapiP2pSyncNodeEntity

@Dao
interface XapiP2pSyncNodeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(
        node: XapiP2pSyncNodeEntity
    )

    @Query("""
        SELECT XapiP2pSyncNodeEntity.*
          FROM XapiP2pSyncNodeEntity
         WHERE XapiP2pSyncNodeEntity.nodeAvailable
    """)
    suspend fun availableNodesAsFlow(): Flow<List<XapiP2pSyncNodeEntity>>

}