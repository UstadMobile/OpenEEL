package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiVerbLangMapEntry

@Dao
interface XapiVerbLangMapEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(list: List<XapiVerbLangMapEntry>)

    @Query(
        """
        SELECT XapiVerbLangMapEntry.*
          FROM XapiVerbLangMapEntry
         WHERE XapiVerbLangMapEntry.vlmeVerbUid = :verbUid
    """
    )
    suspend fun findByVerbUidAsync(verbUid: Long): List<XapiVerbLangMapEntry>

    @Query(
        """
        SELECT XapiVerbLangMapEntry.*
          FROM XapiVerbLangMapEntry
         WHERE XapiVerbLangMapEntry.vlmeVerbUid = :uid1 
            OR XapiVerbLangMapEntry.vlmeVerbUid = :uid2
    """
    )
    suspend fun findByVerbUidPair(uid1: Long, uid2: Long): List<XapiVerbLangMapEntry>


}