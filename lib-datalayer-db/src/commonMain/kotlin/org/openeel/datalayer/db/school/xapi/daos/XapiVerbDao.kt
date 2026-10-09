package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiVerbEntity

@Dao
interface XapiVerbDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<XapiVerbEntity>)

    @Query(
        """
        SELECT XapiVerbEntity.*
        FROM XapiVerbEntity
        WHERE XapiVerbEntity.verbUid = :uid 
    """
    )
    suspend fun findByUid(uid: Long): XapiVerbEntity?

}