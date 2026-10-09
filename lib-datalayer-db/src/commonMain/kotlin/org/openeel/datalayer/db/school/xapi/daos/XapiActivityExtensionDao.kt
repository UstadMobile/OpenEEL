package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityExtensionEntity
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags

@Dao
interface XapiActivityExtensionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(list: List<XapiActivityExtensionEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(list: List<XapiActivityExtensionEntity>)

    @Query(
        """
        DELETE FROM XapiActivityExtensionEntity 
              WHERE aeeActivityUid = :activityUid
    """
    )
    suspend fun deleteByActivityUid(activityUid: Long)

    @Query(
        """
        WITH ActivityUids(activityUid) AS (
             SELECT DISTINCT XapiStatementContextActivityJoin.scajToActivityUid
               FROM XapiStatementContextActivityJoin
              WHERE (XapiStatementContextActivityJoin.scajFromStatementIdHi, XapiStatementContextActivityJoin.scajFromStatementIdLo) IN
                    (SELECT XapiStatementEntity.statementIdHi, XapiStatementEntity.statementIdLo
                       FROM XapiStatementEntity
                      WHERE XapiStatementEntity.statementObjectUid1 = :activityUid
                        AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY})
                        
             )
        
        SELECT XapiActivityExtensionEntity.* 
          FROM XapiActivityExtensionEntity
         WHERE aeeActivityUid IN (SELECT activityUid FROM ActivityUids)
           AND aeeKeyHash = :filterByKeyHash
    """
    )
    suspend fun findAllByActivityContextUids(
        activityUid: Long,
        filterByKeyHash: Long = 0,
    ): List<XapiActivityExtensionEntity>

    @Query("""
        UPDATE XapiActivityExtensionEntity
           SET aeeJson = :json, 
               aeeLastMod = :changeTime
         WHERE aeeActivityUid = :activityUid 
           AND aeeKeyHash = :keyHash
           AND aeeLastMod < :changeTime
    """)
    suspend fun updateIfNewer(
        activityUid: Long,
        keyHash: Long,
        json: String,
        changeTime: Long
    )


}