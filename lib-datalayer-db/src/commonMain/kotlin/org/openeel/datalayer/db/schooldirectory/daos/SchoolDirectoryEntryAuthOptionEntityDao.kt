package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryAuthOptionEntity

@Dao
interface SchoolDirectoryEntryAuthOptionEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: List<SchoolDirectoryEntryAuthOptionEntity>)

    @Query("""
        DELETE FROM SchoolDirectoryEntryAuthOptionEntity
        WHERE sdeAoRdUid = :sdeAoRdUid
    """)
    suspend fun deleteByFk(sdeAoRdUid: Long)

}