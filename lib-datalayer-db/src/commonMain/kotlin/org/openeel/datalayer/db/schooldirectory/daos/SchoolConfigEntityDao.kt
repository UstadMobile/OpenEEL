package world.respect.datalayer.db.schooldirectory.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.schooldirectory.entities.SchoolConfigEntity

@Dao
interface SchoolConfigEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(schoolConfigEntity: SchoolConfigEntity)

    @Query(
        """
        SELECT SchoolConfigEntity.*
          FROM SchoolConfigEntity
         WHERE SchoolConfigEntity.rcUid = :uid 
    """
    )
    suspend fun findByUid(uid: Long): SchoolConfigEntity?

}