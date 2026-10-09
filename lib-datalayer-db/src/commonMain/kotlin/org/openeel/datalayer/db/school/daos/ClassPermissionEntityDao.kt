package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.ClassPermissionEntity

@Dao
interface ClassPermissionEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(permissionsList: List<ClassPermissionEntity>)

    @Query(
        """
        DELETE FROM ClassPermissionEntity
         WHERE ClassPermissionEntity.cpeClassUidNum = :classUidNum
    """
    )
    suspend fun deleteByClassUidNum(classUidNum: Long)

}