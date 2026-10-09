package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.xapi.composites.XapiAssignmentResultRow
import world.respect.datalayer.db.school.xapi.composites.XapiStatementAndJsonEntities
import world.respect.datalayer.db.school.xapi.composites.XapiSubstatementAndVerbEntity
import world.respect.datalayer.db.school.xapi.composites.XapiSummaryResultRow
import world.respect.datalayer.db.school.xapi.composites.XapiTimes
import world.respect.datalayer.db.school.xapi.entities.XapiActivityLangMapEntryPropEnum
import world.respect.datalayer.db.school.xapi.entities.XapiEntityObjectTypeFlags
import world.respect.datalayer.db.school.xapi.entities.XapiStatementContextActivityJoinTypeEnum
import world.respect.datalayer.db.school.xapi.entities.XapiStatementEntity
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.lib.xapi.extensions.reportoptions.StatementReportRow

@Dao
interface XapiStatementEntityDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertOrIgnoreListAsync(entityList: List<XapiStatementEntity>)

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
    ): List<XapiStatementAndJsonEntities>

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
    ): Flow<List<XapiStatementAndJsonEntities>>


    /**
     * When getting a StatementEntity that was used to represent a substatement, if the parent
     * statement existed, it must also exist.
     */
    @Query(
        """
        SELECT XapiStatementEntity.*, XapiVerbEntity.*
          FROM XapiStatementEntity
               JOIN XapiVerbEntity
                    ON (    XapiVerbEntity.verbUid = XapiStatementEntity.statementVerbUid)
         WHERE XapiStatementEntity.statementIdHi = :subStatementIdHi
           AND XapiStatementEntity.statementIdLo = :subStatementIdLo
           AND XapiStatementEntity.isSubStatement
    """
    )
    suspend fun getEntityForSubstatement(
        subStatementIdHi: Long,
        subStatementIdLo: Long,
    ): XapiSubstatementAndVerbEntity

    @Query("""
        SELECT XapiStatementEntity.stored AS timeStored,
               XapiStatementEntity.timestamp AS timestamp
          FROM XapiStatementEntity
         WHERE XapiStatementEntity.statementIdHi = :statementIdHi
           AND XapiStatementEntity.statementIdLo = :statementIdLo
    """)
    suspend fun getTimestampsByUuid(
        statementIdHi: Long,
        statementIdLo: Long,
    ): XapiTimes?

    @Query("""
        SELECT XapiStatementEntity.statementVerbUid
          FROM XapiStatementEntity
         WHERE XapiStatementEntity.statementIdHi = :statementIdHi
           AND XapiStatementEntity.statementIdLo = :statementIdLo
    """)
    suspend fun getVerbUidNumToBeVoided(
        statementIdHi: Long,
        statementIdLo: Long,
    ): Long?

    /**
     * Get a result with one row per combination of assigned actor (including group members) and
     * assigned activity id as per the assignment recipe. Each row includes summary stats for the
     * given combination: if it was completed, successful, progress, score, etc.
     *
     * This is currently based on using an aggregate query and group by to get the required results.
     * An alternative (valid) approach would be to use a CTE based on JOIN to create a row per
     * combination, and then use subqueries. The aggregate functions should be marginally more
     * efficient as it avoids the need to run a separate subquery for each field: the statements can
     * be selected, grouped, and then we can get all the required fields in one go.
     *
     * @param filterByStudentActorUid when not zero, filter to receive only statements from the given
     *        student.
     */
    @Query("""
        WITH LatestAssignmentStatementIds(idHi, idLo, actorUid) AS (
                 SELECT XapiStatementEntity.statementIdHi AS idHi,
                        XapiStatementEntity.statementIdLo AS idLo,
                        CASE(:filterByStudentActorUid)
                            WHEN 0 THEN XapiStatementEntity.statementActorUid
                            ELSE :filterByStudentActorUid
                        END AS actorUid
                   FROM XapiStatementEntity
                  WHERE XapiStatementEntity.statementObjectUid1 = :assignmentActivityIdNum
                    AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY}
                    AND NOT XapiStatementEntity.stmtVoid
               ORDER BY XapiStatementEntity.timestamp DESC
                  LIMIT 1),
             
             AgentActorUids(uid) AS (
                   SELECT (SELECT actorUid FROM LatestAssignmentStatementIds) AS uid
                     UNION
                    SELECT XapiGroupMemberActorJoin.gmajMemberActorUid AS uid
                      FROM XapiGroupMemberActorJoin
                     WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = 
                           (SELECT actorUid FROM LatestAssignmentStatementIds)),
             
             AssignedActivityUids(assignedActivityUid) AS (
                    SELECT XapiStatementContextActivityJoin.scajToActivityUid AS assignedActivityUid
                      FROM XapiStatementContextActivityJoin
                    WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = 
                          (SELECT idHi FROM LatestAssignmentStatementIds)
                      AND XapiStatementContextActivityJoin.scajFromStatementIdLo = 
                          (SELECT idLo FROM LatestAssignmentStatementIds)
                      AND XapiStatementContextActivityJoin.scajContextType = 
                          ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT}    
             )

             -- Use aggregate functions with Group by actor, activity to get the required progress
             -- info per actor and activity.
             SELECT XapiStatementEntity.statementActorUid AS actorUid,
                    XapiStatementEntity.statementObjectUid1 AS activityUid,
                    MAX(XapiStatementEntity.extensionProgress) AS progress,
                    MAX(XapiStatementEntity.resultCompletion) AS resultCompleted,
                    MAX(
                        CASE(XapiStatementEntity.statementVerbUid)
                            WHEN :completeVerbUid THEN 1
                            ELSE NULL
                        END
                    ) AS verbCompleted,
                    MAX(XapiStatementEntity.resultSuccess) AS successful,
                    MAX(XapiStatementEntity.resultScoreScaled) AS scoreScaled
               FROM XapiStatementEntity
              WHERE XapiStatementEntity.statementObjectUid1 IN(
                    SELECT DISTINCT AssignedActivityUids.assignedActivityUid 
                      FROM AssignedActivityUids)
                AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.ACTIVITY}
                AND XapiStatementEntity.statementActorUid IN 
                    (SELECT uid FROM AgentActorUids)
                AND NOT XapiStatementEntity.stmtVoid
                AND :assignmentActivityIdNum IN 
                    (SELECT XapiStatementContextActivityJoin.scajToActivityUid
                       FROM XapiStatementContextActivityJoin
                      WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                        AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo
                        AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT})
           GROUP BY XapiStatementEntity.statementActorUid, XapiStatementEntity.statementObjectUid1
    """)
    suspend fun getAssignmentResults(
        assignmentActivityIdNum: Long,
        completeVerbUid: Long,
        filterByStudentActorUid: Long,
    ): List<XapiAssignmentResultRow>

    /**
     *
     */
    @Query("""
        SELECT XapiStatementEntity.statementObjectUid1 AS activityUid,
               XapiStatementEntity.statementObjectActivityId AS activityId,
               XapiActorEntity.*,
               NameLangMapEntry.almeValue AS title,
               (SELECT COUNT(DISTINCT ProgressStmt.statementObjectUid1)
                  FROM XapiStatementEntity ProgressStmt
                 WHERE ProgressStmt.statementActorUid = :studentAgentActorUid
                   AND $SQL_PROGRESS_STMT_IS_FOR_ASSIGNED_UNIT_CLAUSE
                   AND $SQL_PROGRESS_STMT_INCLUDES_ASSIGNMENT_IN_CONTEXT_ACTIVITIES
                   AND (    ProgressStmt.resultCompletion = 1
                         OR ProgressStmt.statementVerbUid = :completeVerbUid)
                   AND NOT ProgressStmt.stmtVoid      
               ) AS numCompleted,
               (
                    SELECT AVG(ScoreByActivity.resultScoreScaled)
                      -- Get the average of the maximum score per assigned unit activity id
                      FROM (SELECT MAX(ScoreStmt.resultScoreScaled) AS resultScoreScaled
                              FROM XapiStatementEntity ScoreStmt
                             WHERE ScoreStmt.statementActorUid = :studentAgentActorUid
                               AND XapiStatementEntity.statementObjectUid1 IN (
                                    SELECT XapiStatementContextActivityJoin.scajToActivityUid
                                      FROM XapiStatementContextActivityJoin
                                     WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = ScoreStmt.statementIdHi
                                       AND XapiStatementContextActivityJoin.scajFromStatementIdLo = ScoreStmt.statementIdLo
                                       AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT})
                               AND ScoreStmt.resultScoreScaled IS NOT NULL
                          GROUP BY ScoreStmt.statementObjectUid1) AS ScoreByActivity
               ) AS averageScoreScaled,
               (SELECT COUNT(*) 
                  FROM XapiStatementContextActivityJoin
                 WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                   AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo
                   AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT}) AS numTotal,
               DeadlineExtensionEntity.aeeJson AS deadlineStr
          FROM XapiStatementEntity
               $SQL_JOIN_ASSIGNMENT_SUMMARY
         WHERE XapiStatementEntity.statementVerbUid = :assignVerbUid
           AND (      :studentAgentActorUid = XapiStatementEntity.statementActorUid
                 OR (:studentAgentActorUid IN 
                     (SELECT XapiGroupMemberActorJoin.gmajMemberActorUid
                       FROM XapiGroupMemberActorJoin
                      WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = XapiStatementEntity.statementActorUid)))   
           AND $SQL_STATEMENT_ENTITY_IS_MOST_RECENT_FOR_OBJECT_CLAUSE
    """)
    fun getAssignmentListForStudentAsFlow(
        assignVerbUid: Long,
        studentAgentActorUid: Long,
        completeVerbUid: Long,
        deadlineExtensionHash: Long,
    ): Flow<List<XapiSummaryResultRow>>

    @Query("""
        SELECT XapiStatementEntity.statementObjectUid1 AS activityUid,
               XapiStatementEntity.statementObjectActivityId AS activityId,
               XapiActorEntity.*,
               NameLangMapEntry.almeValue AS title,
               (SELECT COUNT(DISTINCT XapiActorInner.actorUid)
                  FROM XapiActorEntity XapiActorInner
                        -- Select ActorEntities that are assignees
                 WHERE (    (     XapiActorInner.actorUid = XapiStatementEntity.statementActorUid
                              AND XapiActorInner.actorObjectType = ${XapiEntityObjectTypeFlags.AGENT})
                         OR XapiActorInner.actorUid IN 
                            (SELECT XapiGroupMemberActorJoin.gmajMemberActorUid
                               FROM XapiGroupMemberActorJoin
                              WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = XapiStatementEntity.statementActorUid)
                       )
                       -- Filter actors by those where the total of distinct completed statements
                       -- equals the total  
                       -- units for this assignment Do we need to double check the activity uid here?
                   AND (
                        (SELECT COUNT(DISTINCT ProgressStmt.statementObjectUid1)
                           FROM XapiStatementEntity ProgressStmt
                          WHERE ProgressStmt.statementActorUid = XapiActorInner.actorUid
                            AND ProgressStmt.statementVerbUid = :completedVerbUid
                            AND $SQL_PROGRESS_STMT_IS_FOR_ASSIGNED_UNIT_CLAUSE
                                -- Check that the progress statement includes the assignment activity id 
                                -- in its context grouping activities    
                            AND $SQL_PROGRESS_STMT_INCLUDES_ASSIGNMENT_IN_CONTEXT_ACTIVITIES       
                            AND NOT ProgressStmt.stmtVoid
                        ) = (SELECT COUNT(*)
                               FROM XapiStatementContextActivityJoin 
                              WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                                AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo
                                AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT})
                   )
               
               ) AS numCompleted,
               (SELECT COUNT(*)
                  FROM XapiActorEntity XapiActorInner
                 WHERE (     XapiActorInner.actorUid = XapiStatementEntity.statementActorUid
                         AND XapiActorInner.actorObjectType = ${XapiEntityObjectTypeFlags.AGENT})
                   OR XapiActorInner.actorUid IN 
                      (SELECT XapiGroupMemberActorJoin.gmajMemberActorUid
                         FROM XapiGroupMemberActorJoin
                        WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = XapiStatementEntity.statementActorUid)    
               ) AS numTotal,
               DeadlineExtensionEntity.aeeJson AS deadlineStr,
               NULL AS averageScoreScaled
          FROM XapiStatementEntity
               $SQL_JOIN_ASSIGNMENT_SUMMARY
         WHERE XapiStatementEntity.statementVerbUid = :assignVerbUid   
           AND :assignmentRecipeActivityUid IN (
               SELECT XapiStatementContextActivityJoin.scajToActivityUid
                 FROM XapiStatementContextActivityJoin
                WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                  AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo
                  AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.CATEGORY_FLAG_INT})
           AND $SQL_STATEMENT_ENTITY_IS_MOST_RECENT_FOR_OBJECT_CLAUSE
    """)
    fun getAssignmentListAsFlow(
        assignmentRecipeActivityUid: Long,
        assignVerbUid: Long,
        completedVerbUid: Long,
        deadlineExtensionHash: Long,
    ): Flow<List<XapiSummaryResultRow>>

    @Query("""
        UPDATE XapiStatementEntity
           SET stmtVoid = 1
         WHERE XapiStatementEntity.statementIdHi = :voidStmtIdHi
           AND XapiStatementEntity.statementIdLo = :voidStmtIdLo
    """)
    suspend fun updateSetStatementVoided(
        voidStmtIdHi: Long,
        voidStmtIdLo: Long
    )

    @RawQuery
    suspend fun runReportQuery(query: RoomRawQuery): List<StatementReportRow>

    companion object {

        const val SINCE_UNSET = Long.MIN_VALUE

        const val UNTIL_UNSET = Long.MAX_VALUE

        /**
         * Check that progress statement is for a unit activity id that is
         * an assigned activity id as per the xapi statement for the assignment
         * itself
         */
        const val SQL_PROGRESS_STMT_IS_FOR_ASSIGNED_UNIT_CLAUSE = """
            ProgressStmt.statementObjectUid1 IN (
                SELECT XapiStatementContextActivityJoin.scajToActivityUid
                  FROM XapiStatementContextActivityJoin
                 WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = XapiStatementEntity.statementIdHi
                   AND XapiStatementContextActivityJoin.scajFromStatementIdLo = XapiStatementEntity.statementIdLo
                   AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT})
        """

        const val SQL_PROGRESS_STMT_INCLUDES_ASSIGNMENT_IN_CONTEXT_ACTIVITIES = """
            EXISTS(
                    SELECT 1
                      FROM XapiStatementContextActivityJoin
                     WHERE XapiStatementContextActivityJoin.scajFromStatementIdHi = ProgressStmt.statementIdHi
                       AND XapiStatementContextActivityJoin.scajFromStatementIdLo = ProgressStmt.statementIdLo
                       AND XapiStatementContextActivityJoin.scajToActivityUid = XapiStatementEntity.statementObjectUid1
                       AND XapiStatementContextActivityJoin.scajContextType = ${XapiStatementContextActivityJoinTypeEnum.GROUP_FLAG_INT})
        """

        const val SQL_JOIN_ASSIGNMENT_SUMMARY = """
               LEFT JOIN XapiActivityLangMapEntry NameLangMapEntry 
                    ON (NameLangMapEntry.almeActivityUid, NameLangMapEntry.almeKeyHash) IN 
                       (SELECT XapiActivityLangMapEntry.almeActivityUid,
                               XapiActivityLangMapEntry.almeKeyHash
                          FROM XapiActivityLangMapEntry
                         WHERE XapiActivityLangMapEntry.almeActivityUid = XapiStatementEntity.statementObjectUid1
                           AND XapiActivityLangMapEntry.almeProperty = ${XapiActivityLangMapEntryPropEnum.NAME_FLAG_INT}
                         LIMIT 1)
               JOIN XapiActorEntity
                    ON XapiStatementEntity.statementActorUid = XapiActorEntity.actorUid
               LEFT JOIN XapiActivityExtensionEntity DeadlineExtensionEntity
                         ON (DeadlineExtensionEntity.aeeActivityUid = XapiStatementEntity.statementObjectUid1
                             AND DeadlineExtensionEntity.aeeKeyHash = :deadlineExtensionHash)
        """

        const val SQL_STATEMENT_ENTITY_IS_MOST_RECENT_FOR_OBJECT_CLAUSE = """
            (XapiStatementEntity.statementIdHi, XapiStatementEntity.statementIdLo) IN
               (SELECT XapiStatementInner.statementIdHi, 
                       XapiStatementInner.statementIdLo
                  FROM XapiStatementEntity XapiStatementInner
                 WHERE XapiStatementInner.statementObjectUid1 = XapiStatementEntity.statementObjectUid1
                   AND XapiStatementInner.statementObjectType = XapiStatementEntity.statementObjectType
              ORDER BY XapiStatementInner.timestamp DESC
                 LIMIT 1)
        """

        const val XAPI_STATEMENT_PERMISSION_CLAUSE = """
            (   (SELECT EXISTS(
                        SELECT 1
                          FROM PersonRoleEntity
                         WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum
                           AND (     PersonRoleEntity.prRoleEnum = ${PersonRoleEnum.TEACHER_INT}
                                  OR PersonRoleEntity.prRoleEnum = ${PersonRoleEnum.SYSTEM_ADMINISTRATOR_INT}
                                  OR PersonRoleEntity.prRoleEnum = ${PersonRoleEnum.SITE_ADMINISTRATOR_INT})))
                 OR (:authenticatedActorUid = XapiStatementEntity.statementActorUid)
                 OR (     :authenticatedActorUid = XapiStatementEntity.statementObjectUid1
                      AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.AGENT})
                 OR (     XapiStatementEntity.statementVerbUid = :appListingVerbUid)     
                 OR (:authenticatedActorUid IN 
                     (SELECT XapiGroupMemberActorJoin.gmajMemberActorUid
                        FROM XapiGroupMemberActorJoin
                       WHERE XapiGroupMemberActorJoin.gmajGroupActorUid = XapiStatementEntity.statementActorUid
                          OR (     XapiGroupMemberActorJoin.gmajGroupActorUid = XapiStatementEntity.statementObjectUid1
                               AND XapiStatementEntity.statementObjectType = ${XapiEntityObjectTypeFlags.GROUP}))
                    )          
               )
        """

        /*
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
            
            
        SELECT XapiStatementEntity.*, XapiVerbEntity.*
          FROM XapiStatementEntity
               LEFT JOIN XapiVerbEntity
                    ON (    XapiVerbEntity.verbUid = XapiStatementEntity.statementVerbUid)
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