package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiSessionEntity

@Dao
interface XapiSessionEntityDao {

    @Insert
    suspend fun insertAsync(xapiSessionEntity: XapiSessionEntity): Long

    @Query(
        """
        SELECT XapiSessionEntity.*
          FROM XapiSessionEntity
         WHERE XapiSessionEntity.xseUid = :uid
    """
    )
    suspend fun findByUidAsync(uid: Long): XapiSessionEntity?

}