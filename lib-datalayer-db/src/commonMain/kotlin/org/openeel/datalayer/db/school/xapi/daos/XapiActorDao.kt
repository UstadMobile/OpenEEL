package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActorEntity

@Dao
interface XapiActorDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<XapiActorEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(entities: List<XapiActorEntity>)

    @Query(
        """
        UPDATE XapiActorEntity
           SET actorName = :name,
               actorLastModified = :updateTime
         WHERE actorUid = :uid
           AND XapiActorEntity.actorName != :name
    """
    )
    suspend fun updateIfNameChanged(
        uid: Long,
        name: String?,
        updateTime: Long,
    )

    @Query(
        """
        SELECT XapiActorEntity.*
          FROM XapiActorEntity
         WHERE XapiActorEntity.actorUid = :uid
    """
    )
    suspend fun findByUidAsync(uid: Long): XapiActorEntity?

    @Query(
        """
        UPDATE XapiActorEntity
           SET actorGroupMembersLastUpdated = :updateTime
         WHERE actorUid = :actorUid
    """
    )
    suspend fun updateGroupMembersLastUpdated(
        actorUid: Long,
        updateTime: Long,
    )


    @Query(
        """
        SELECT XapiActorEntity.*
          FROM XapiActorEntity
         WHERE XapiActorEntity.actorUid IN (:uids)
            OR XapiActorEntity.actorUid IN (
               SELECT XapiGroupMemberActorJoin.gmajMemberActorUid
                 FROM XapiGroupMemberActorJoin
                WHERE XapiGroupMemberActorJoin.gmajGroupActorUid IN (:uids))
    """
    )
    suspend fun findByUidList(
        uids: List<Long>
    ): List<XapiActorEntity>


}