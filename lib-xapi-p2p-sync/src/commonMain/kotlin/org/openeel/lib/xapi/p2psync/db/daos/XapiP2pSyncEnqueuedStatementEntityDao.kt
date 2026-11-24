package org.openeel.lib.xapi.p2psync.db.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy

@Dao
interface XapiP2pSyncEnqueuedStatementEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(entities: List<XapiP2pSyncEnqueuedStatementEntityDao>)

}