package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao.Companion.SINCE_UNSET
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao.Companion.UNTIL_UNSET
import world.respect.datalayer.db.school.xapi.daos.XapiStatementEntityDao.Companion.XAPI_STATEMENT_PERMISSION_CLAUSE
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntityJson

@Dao
interface XapiStatementEntityJsonDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<XapiStatementEntityJson>)

    @Query(LIST_SQL)
    suspend fun list(
        statementIdHi: Long,
        statementIdLo: Long,
        voidedStatementIdHi: Long,
        voidedStatementIdLo: Long,
        agentUid: Long,
        verbUid: Long,
        activityUid: Long,
        relatedAgents: Boolean,
        relatedActivities: Boolean,
        since: Long,
        until: Long,
        ascending: Boolean,
        limit: Int,
        authenticatedPersonUidNum: Long,
        authenticatedActorUid: Long,
        appListingVerbUid: Long,
    ): List<XapiStatementEntityJson>


    @Query(LIST_SQL)
    fun listAsFlow(
        statementIdHi: Long,
        statementIdLo: Long,
        voidedStatementIdHi: Long,
        voidedStatementIdLo: Long,
        agentUid: Long,
        verbUid: Long,
        activityUid: Long,
        relatedAgents: Boolean,
        relatedActivities: Boolean,
        since: Long,
        until: Long,
        ascending: Boolean,
        limit: Int,
        authenticatedPersonUidNum: Long,
        authenticatedActorUid: Long,
        appListingVerbUid: Long,
    ): Flow<List<XapiStatementEntityJson>>

    @Query(
        """
        SELECT XapiStatementEntityJson.*
          FROM XapiStatementEntityJson
         WHERE (    (:stmtJsonIdHi = 0 AND :stmtJsonIdLo = 0) 
                 OR (stmtJsonIdHi = :stmtJsonIdHi AND stmtJsonIdLo = :stmtJsonIdLo))
                  
    """
    )
    suspend fun getStatements(
        stmtJsonIdHi: Long,
        stmtJsonIdLo: Long,
    ): List<XapiStatementEntityJson>



    companion object {

        /**
        Begin statement query : This query is the same for both XapiStatementEntity
        based return results (used for canonical and id results) and exact (which uses
        XapiStatementEntityJson).

        Normally: it would be better to handle this as a constant: however because its only in
        two places and syntax highlighting is important for it, it is copy/pasted.

         */
        // language=RoomSql
        const val LIST_SQL = """
            -- Get a list of all the actors that should be considered as related to agent parameter
            -- Eg the actor uid itself and any group members
          WITH AgentActorUids(uid) AS
               (SELECT :agentUid AS uid
                 UNION
                SELECT XapiGroupMemberActorJoin.gmajGroupActorUid AS uid
                  FROM XapiGroupMemberActorJoin
                 WHERE :agentUid != 0 
                   AND XapiGroupMemberActorJoin.gmajMemberActorUid = :agentUid)
            
            
        SELECT XapiStatementEntityJson.*
          FROM XapiStatementEntity
               JOIN XapiStatementEntityJson
                    ON (    XapiStatementEntityJson.stmtJsonIdHi = XapiStatementEntity.statementIdHi
                        AND XapiStatementEntityJson.stmtJsonIdLo = XapiStatementEntity.statementIdLo)
               LEFT JOIN XapiStatementEntity AS SubStatementEntity
                    ON (    XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.SUBSTATEMENT}
                        AND SubStatementEntity.statementIdHi = XapiStatementEntity.statementObjectUid1
                        AND SubStatementEntity.statementIdLo = XapiStatementEntity.statementObjectUid2)
         WHERE (   (:statementIdHi = 0 AND :statementIdLo = 0) 
                OR (     XapiStatementEntity.statementIdHi = :statementIdHi 
                     AND XapiStatementEntity.statementIdLo = :statementIdLo))
           AND (   (:voidedStatementIdHi = 0 AND :voidedStatementIdLo = 0)
                OR (     XapiStatementEntity.statementIdHi = :voidedStatementIdHi 
                     AND XapiStatementEntity.statementIdLo = :voidedStatementIdLo
                     AND XapiStatementEntity.stmtVoid))
           AND (:since = $SINCE_UNSET OR XapiStatementEntity.stored > :since)
           AND (:until = $UNTIL_UNSET OR XapiStatementEntity.stored <= :until)
           -- Handle agent parameter
           AND (    :agentUid = 0 
                  OR (    XapiStatementEntity.statementActorUid IN 
                          (SELECT uid FROM AgentActorUids))
                  OR (    (    XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.AGENT}
                            OR XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.GROUP})
                      AND (XapiStatementEntity.statementObjectUid1 IN (SELECT uid FROM AgentActorUids)))
                  -- Handle related agents        
                  OR (    :relatedAgents 
                      AND (    XapiStatementEntity.authorityActorUid IN (SELECT uid FROM AgentActorUids)
                            OR XapiStatementEntity.contextTeamActorUid IN (SELECT uid FROM AgentActorUids)
                            OR XapiStatementEntity.contextInstructorActorUid IN (SELECT uid FROM AgentActorUids)
                            -- check substatement if applicable
                            OR (     SubStatementEntity.statementActorUid IS NOT NULL
                                AND (    SubStatementEntity.statementActorUid IN (SELECT uid FROM AgentActorUids)
                                      OR (     (   SubStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.AGENT}
                                                OR SubStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.GROUP})
                                           AND (SubStatementEntity.statementObjectUid1 IN (SELECT uid FROM AgentActorUids)))
                                           
                                      OR (SubStatementEntity.authorityActorUid IN (SELECT uid FROM AgentActorUids))
                                      OR (SubStatementEntity.contextTeamActorUid IN (SELECT uid FROM AgentActorUids))
                                      OR (SubStatementEntity.contextInstructorActorUid IN (SELECT uid FROM AgentActorUids))  
                                    )
                               )       
                          )
                     )
               )
           AND (:verbUid = 0 OR XapiStatementEntity.statementVerbUid = :verbUid)
               -- Handle activity parameter
           AND (      :activityUid = 0
                   OR (     XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY}
                       AND XapiStatementEntity.statementObjectUid1 = :activityUid)
                   OR (     :relatedActivities    
                        AND (    -- As per spec check if substatement activity matches when relatedActivities is set  
                                 (     SubStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY} 
                                   AND SubStatementEntity.statementObjectUid1 = :activityUid)
                                 -- As per spec check if activity uid is part included in context activities.   
                               OR (:activityUid IN 
                                   (SELECT XapiStatementContextActivityJoin.scajToActivityUid
                                      FROM XapiStatementContextActivityJoin
                                     WHERE (    XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                                            AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo)
                                        OR (    SubStatementEntity.statementIdHi IS NOT NULL 
                                            AND XapiStatementContextActivityJoin.scajFromStatementIdHi = SubStatementEntity.statementIdHi
                                            AND XapiStatementContextActivityJoin.scajFromStatementIdLo = SubStatementEntity.statementIdLo)     
                                   )
                                  )    
                            )
                      ) 
               )          
           AND NOT XapiStatementEntity.isSubStatement
           AND (    NOT XapiStatementEntity.stmtVoid
                 OR (:voidedStatementIdHi != 0 AND :voidedStatementIdLo != 0))
           AND $XAPI_STATEMENT_PERMISSION_CLAUSE      
      ORDER BY CASE(:ascending) WHEN 1 THEN XapiStatementEntity.stored END ASC,
               CASE(:ascending) WHEN 0 THEN XapiStatementEntity.stored END DESC
         LIMIT :limit
        """

    }
}