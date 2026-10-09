package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.schooldirectory.adapters.SchoolDirectoryEntryEntities
import world.respect.datalayer.db.schooldirectory.entities.SchoolDirectoryEntryEntity

@Dao
interface SchoolDirectoryEntryEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(realmEntity: SchoolDirectoryEntryEntity)

    @Query(
        """
        SELECT * 
         FROM SchoolDirectoryEntryEntity
        WHERE reUid = :uid
    """
    )
    suspend fun findByUid(uid: Long): SchoolDirectoryEntryEntities?

    @Transaction
    @Query(SELECT_LIST_SQL)
    fun listAsFlow(
        name: String?,
        directoryUrl: String?,
    ): Flow<List<SchoolDirectoryEntryEntities>>


    @Transaction
    @Query(SELECT_LIST_SQL)
    suspend fun list(
        name: String?,
        directoryUrl: String?,
    ): List<SchoolDirectoryEntryEntities>


    companion object {

        const val SELECT_LIST_SQL = """
        SELECT SchoolDirectoryEntryEntity.*
          FROM SchoolDirectoryEntryEntity
         WHERE (:name IS NULL OR SchoolDirectoryEntryEntity.reUid IN
                (SELECT SchoolDirectoryEntryLangMapEntity.sdelReUid
                   FROM SchoolDirectoryEntryLangMapEntity
                  WHERE SchoolDirectoryEntryLangMapEntity.sdelValue LIKE :name))
           AND (    :directoryUrl IS NULL
                 OR SchoolDirectoryEntryEntity.reInDirectoryUrl = :directoryUrl)         
        """

    }


}