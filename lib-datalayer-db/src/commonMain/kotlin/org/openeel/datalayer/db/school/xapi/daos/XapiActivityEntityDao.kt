package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.adapters.XapiActivityEntities
import world.respect.datalayer.db.school.xapi.entities.XapiActivityEntity

@Dao
interface XapiActivityEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreAsync(entities: List<XapiActivityEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(xapiActivityEntity: XapiActivityEntity)

    @Query("""
        SELECT XapiActivityEntity.*
          FROM XapiActivityEntity
         WHERE XapiActivityEntity.actUid IN (:activityUids)
    """)
    suspend fun findByUidList(activityUids: List<Long>): List<XapiActivityEntities>


    @Query(
        """
        UPDATE XapiActivityEntity
           SET actMoreInfo = :actMoreInfo,
               actNonSignificantLastModified = :actLct
        WHERE actUid = :activityUid
          AND actMoreInfo != :actMoreInfo      
    """
    )
    suspend fun updateIfMoreInfoChanged(
        activityUid: Long,
        actMoreInfo: String?,
        actLct: Long,
    )

    @Query(
        """
        SELECT XapiActivityEntity.*
          FROM XapiActivityEntity
         WHERE XapiActivityEntity.actUid = :activityUid 
    """
    )
    suspend fun findByUidAsync(activityUid: Long): XapiActivityEntity?

    @Query(
        """
        SELECT XapiActivityEntity.*
          FROM XapiActivityEntity
         WHERE XapiActivityEntity.actUid = :activityUid 
    """
    )
    suspend fun getEntitiesByUid(activityUid: Long): XapiActivityEntities?


}