package org.openeel.lib.xapi.p2psync.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.RoomDatabaseConstructor
import org.openeel.lib.xapi.p2psync.db.daos.XapiP2pSyncEnqueuedStatementEntityDao
import org.openeel.lib.xapi.p2psync.db.daos.XapiP2pSyncNodeEntityDao
import org.openeel.lib.xapi.p2psync.db.entities.XapiP2pSyncNodeEntity

@Database(
    entities =[
        XapiP2pSyncEnqueuedStatementEntity::class,
        XapiP2pSyncNodeEntity::class,
    ],
    version = 1,
)
internal abstract class XapiP2pSyncDb: RoomDatabase() {

    //List of statements to push essentially
    abstract fun enqueuedStatementEntityDao(): XapiP2pSyncEnqueuedStatementEntityDao

    abstract fun nodeEntityDao(): XapiP2pSyncNodeEntityDao

}


// The Room compiler generates the `actual` implementations.
@Suppress("NO_ACTUAL_FOR_EXPECT", "EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING",
    "KotlinNoActualForExpect", "RedundantSuppression"
)
internal expect object XapiP2pSyncDbbConstructor: RoomDatabaseConstructor<XapiP2pSyncDb> {
    override fun initialize(): XapiP2pSyncDb
}
