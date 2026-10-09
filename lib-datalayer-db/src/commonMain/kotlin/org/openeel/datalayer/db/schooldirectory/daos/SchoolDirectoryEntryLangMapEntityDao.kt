package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryLangMapEntity

@Dao
interface SchoolDirectoryEntryLangMapEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: List<SchoolDirectoryEntryLangMapEntity>)

    @Query("""
        DELETE FROM SchoolDirectoryEntryLangMapEntity 
         WHERE sdelReUid = :sdelReUid
    """)
    suspend fun deleteByFk(sdelReUid: Long)


}