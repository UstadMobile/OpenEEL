package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityInteractionEntity

@Dao
interface XapiActivityInteractionDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<XapiActivityInteractionEntity>)


    @Query(
        """
        DELETE FROM XapiActivityInteractionEntity 
         WHERE aieActivityUid = :activityUid
    """
    )
    suspend fun deleteByActivityUid(activityUid: Long)

    @Query(
        """
        SELECT XapiActivityInteractionEntity.*
          FROM XapiActivityInteractionEntity
         WHERE XapiActivityInteractionEntity.aieActivityUid = :activityUid 
    """
    )
    suspend fun findAllByActivityUidAsync(
        activityUid: Long
    ): List<XapiActivityInteractionEntity>



}