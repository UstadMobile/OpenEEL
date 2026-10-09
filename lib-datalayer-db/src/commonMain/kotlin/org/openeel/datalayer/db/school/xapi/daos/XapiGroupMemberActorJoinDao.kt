package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiGroupMemberActorJoin

@Dao
interface XapiGroupMemberActorJoinDao {


    @Query(
        """
        DELETE FROM XapiGroupMemberActorJoin
         WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = :groupActorUid
    """
    )
    suspend fun deleteByGroupActorUidAsync(groupActorUid: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entities: List<XapiGroupMemberActorJoin>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertListAsync(entities: List<XapiGroupMemberActorJoin>)

    /**
     * @param uidList a list of the actoruids for the groups for which we should retrieve members
     * @param excludeIdentifiedGroups if true, exclude results for members of identified groups (
     *        e.g. when running an ids only format query)
     */
    @Query(
        """
        SELECT XapiGroupMemberActorJoin.*
          FROM XapiGroupMemberActorJoin
         WHERE XapiGroupMemberActorJoin.gmajGroupActorUid IN (:uidList)
           AND (    :excludeIdentifiedGroups = 0
                 OR (SELECT XapiActorEntity.actorIsAnonGroup
                       FROM XapiActorEntity
                      WHERE XapiActorEntity.actorUid = XapiGroupMemberActorJoin.gmajGroupActorUid) = 1)
    """
    )
    suspend fun findByGroupActorUidList(
        uidList: List<Long>,
        excludeIdentifiedGroups: Boolean,
    ): List<XapiGroupMemberActorJoin>


}