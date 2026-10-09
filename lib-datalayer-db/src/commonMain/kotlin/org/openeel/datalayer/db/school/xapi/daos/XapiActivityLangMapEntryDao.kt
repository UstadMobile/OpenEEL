package com.ustadmobile.core.db.dao.xapi

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityLangMapEntry

@Dao
interface XapiActivityLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(entities: List<XapiActivityLangMapEntry>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnore(entities: List<XapiActivityLangMapEntry>)

    @Query(
        """
        UPDATE XapiActivityLangMapEntry
           SET almeValue = :almeValue,
               almeLastModified = :changeTime
         WHERE almeActivityUid = :almeActivityUid
           AND almeKeyHash = :almeKeyHash
           AND almeLastModified < :changeTime
           AND almeValue != :almeValue
    """
    )
    suspend fun updateIfChanged(
        almeActivityUid: Long,
        almeKeyHash: Long,
        almeValue: String,
        changeTime: Long,
    )

    @Query(
        """
        SELECT XapiActivityLangMapEntry.*
          FROM XapiActivityLangMapEntry
         WHERE XapiActivityLangMapEntry.almeActivityUid = :activityUid
    """
    )
    suspend fun findAllByActivityUid(activityUid: Long): List<XapiActivityLangMapEntry>


}