package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.LastModifiedAndPermission
import world.respect.datalayer.db.school.entities.PersonEntity
import world.respect.datalayer.db.school.entities.PersonEntityWithRoles
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.PersonStatusEnum
import world.respect.datalayer.school.model.StatusEnum
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.libutil.util.time.TimeConstants
import world.respect.libutil.util.time.systemTimeInMillis

@Dao
interface PersonEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(personEntity: PersonEntity)

    @Query("""
        SELECT PersonEntity.pLastModified
          FROM PersonEntity
         WHERE PersonEntity.pGuidHash = :guidHash
         LIMIT 1
    """)
    suspend fun getLastModifiedByGuid(guidHash: Long): Long?


    /**
     * Query to determine if a user has a given (normally write related permission) for another user.
     * Determining if a user has a given permission over another user requires knowing the role of
     * the other user (eg. PERSON_TEACHER_WRITE vs. PERSON_PARENT_WRITE etc).
     *
     * This function can be used in two scenarios:
     * a) Before the person has been written to the database, however the role is known (eg in the
     *    PersonEdit screen): the knownRoleFlag can be passed to allow the query to determine the
     *    appropriate permission flag.
     * b) After the person has been written to the database, however the ViewModel does not
     *    explicitly know the role e.g. the PersonView screen when it needs to check if the
     *    authenticated user has permission to edit the person being displayed.
     *
     * @param knownRoleFlag .
     */
    @Query(SELECT_PERMISSION_AND_LAST_MODIFIED_SQL)
    suspend fun getLastModifiedAndHasPermission(
        authenticatedPersonUidNum: Long,
        personUidNum: Long,
        knownRoleFlag: Int,
        roleAdminPermissionRequired: Long = PermissionFlags.PERSON_ADMIN_WRITE,
        roleTeacherPermissionRequired: Long = PermissionFlags.PERSON_TEACHER_WRITE,
        roleStudentPermissionRequired: Long = PermissionFlags.PERSON_STUDENT_WRITE,
        roleParentPermissionRequired: Long = PermissionFlags.PERSON_PARENT_WRITE,
        roleSharedDevicePermissionRequired: Long = PermissionFlags.PERSON_STUDENT_READ,
    ): LastModifiedAndPermission


    @Transaction
    @Query("""
        SELECT * 
         FROM PersonEntity
        WHERE pUsername = :username
    """)
    suspend fun findByUsername(username: String): PersonEntityWithRoles?

    @Transaction
    @Query("""
       SELECT * 
         FROM PersonEntity
        WHERE pGuidHash = :guidHash
    """)
    suspend fun findByGuidNum(guidHash: Long): PersonEntityWithRoles?

    @Transaction
    @Query("""
        SELECT * 
         FROM PersonEntity
        WHERE pGuidHash = :guidHash
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<PersonEntityWithRoles?>

    @Query("""
        SELECT PersonEntity.pGuid AS guid, 
               PersonEntity.pGivenName AS givenName, 
               PersonEntity.pFamilyName AS familyName, 
               PersonEntity.pUsername AS username
          FROM PersonEntity
    """)
    fun findAllListDetailsAsFlow(): Flow<List<PersonListDetails>>

    @Transaction
    @Query("""
           WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
                
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    fun listAsFlow(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        inClassOnDayInUtcMs: Long = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        filterByPersonStatus: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
        excludeSharedSchoolDevice: Boolean = false,
    ): Flow<List<PersonEntityWithRoles>>

    @Query("""
           WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    suspend fun list(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        inClassOnDayInUtcMs: Long = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        filterByPersonStatus: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
        excludeSharedSchoolDevice: Boolean = false,
    ): List<PersonEntityWithRoles>

    @Transaction
    @Query("""
        SELECT PersonEntity.*
          FROM PersonEntity
         WHERE PersonEntity.pGuidHash IN (:uidNums) 
    """)
    suspend fun findByUidList(uidNums: List<Long>): List<PersonEntityWithRoles>


    /**
     * @param inClassOnDayInUtcMs if filtering by clazzUid, and we want only those who have an active
     *        enrollment on a given day (e.g. today as per the users timezone), then we need to know
     *        what day that should be. LocalDate is stored as millis since epoch until 00:00 UTC for
     *        the given date. See the docs for startOfTodaysDateInMillisAtUtc function.
     */
    @Transaction
    @Query("""
           WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL,  
                $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                $LIST_PERSONS_CTES_SQL
                         
         SELECT PersonEntity.*
           FROM PersonEntity
          WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
          ORDER BY PersonEntity.pGivenName
    """)
    fun listAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClassOnDayInUtcMs: Long = 0,
        inClazzRoleFlag: Int = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        filterByPersonStatus: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
        excludeSharedSchoolDevice: Boolean = false,
    ): PagingSource<Int, PersonEntityWithRoles>

    @Query("""
          WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL,  
               $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
               $LIST_PERSONS_CTES_SQL
        
        SELECT PersonEntity.pGuid AS guid, 
               PersonEntity.pGivenName AS givenName, 
               PersonEntity.pFamilyName AS familyName, 
               PersonEntity.pUsername AS username,
               PersonRoleEntity.prRoleEnum AS role
          FROM PersonEntity
               JOIN PersonRoleEntity
                    ON PersonRoleEntity.prUid = 
                       (SELECT PersonRoleEntity.prUid
                          FROM PersonRoleEntity
                         WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash
                           AND PersonRoleEntity.prIsPrimaryRole
                         LIMIT 1)
         WHERE PersonEntity.pGuidHash IN (
                SELECT DISTINCT uidNum
                  FROM AllPersons)
      ORDER BY PersonEntity.pGivenName
    """)
    fun findAllListDetailsAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        inClazzGuidHash: Long = 0,
        inClazzRoleFlag: Int = 0,
        inClassOnDayInUtcMs: Long = 0,
        filterByName: String? = null,
        timeNow: Long = systemTimeInMillis(),
        filterByPersonRole: Int = 0,
        filterByPersonStatus: Int = 0,
        includeRelated: Boolean = false,
        includeDeleted: Boolean = false,
        excludeSharedSchoolDevice: Boolean = false,
    ): PagingSource<Int, PersonListDetails>

    @Query("""
            SELECT * 
            FROM PersonEntity
            WHERE pGuid = :sourcedId
            """
    )
    suspend fun getAllUsers(sourcedId: String): List<PersonEntity>

    /**
     * Determine the most recent time that a person's permissions have changed. Used to determine
     * the since validation key. See PullSyncTracker for more details.
     */
    @Query("""
        WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL 
      SELECT MAX(
               -- Most recent change to schoolpermissiongrant for the given person
               COALESCE(
                   (SELECT MAX(SchoolPermissionGrantEntity.spgStored)
                      FROM SchoolPermissionGrantEntity
                     WHERE SchoolPermissionGrantEntity.spgToRole IN
                           (SELECT PersonRoleEntity.prRoleEnum
                              FROM PersonRoleEntity
                             WHERE PersonRoleEntity.prPersonGuidHash IN 
                                   ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL))), 
                   0
               ),
               -- Most recent change to enrollment that will also affect permissions for person
               COALESCE(
                   (SELECT MAX(EnrollmentEntity.eStored)
                      FROM EnrollmentEntity
                     WHERE EnrollmentEntity.ePersonUidNum IN 
                           ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL)),
                   0
               )
             )
    """)
    suspend fun getMostRecentPermissionChangeTime(
        authenticatedPersonUidNum: Long
    ): Long

    @Query("""
        SELECT EXISTS(
                SELECT 1 
                  FROM PersonEntity
                 WHERE PersonEntity.pUsername = :username)
    """)
    suspend fun getUsernameAlreadyExists(username: String): Boolean


    companion object {

        /**
         * CTE of PersonUids applicable for the authenticated person: always includes the person uid
         * for the authenticated person. If the authenticated user is a parent, then this will also
         * include the personuids for their children (e.g. so the parent will have any permission
         * the child would have, but not vice versa).
         */
        const val AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL = """
            AuthenticatedPermissionPersonUids(uidNum) AS (
                SELECT :authenticatedPersonUidNum AS uidNum
                UNION
                SELECT PersonRelatedPersonEntity.prpOtherPersonUidNum AS uidNum
                  FROM PersonRelatedPersonEntity
                 WHERE ${PersonRoleEnum.PARENT_INT} IN 
                       (SELECT PersonRoleEntity.prRoleEnum
                          FROM PersonRoleEntity
                         WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum)
                   AND PersonRelatedPersonEntity.prpPersonUidNum = :authenticatedPersonUidNum)     
        """

        /* Commonly used expression to get a list of personuids for the authenticated person from the
         * above CTE
         */
        const val SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL = """
            SELECT AuthenticatedPermissionPersonUids.uidNum
              FROM AuthenticatedPermissionPersonUids
        """



        /**
         * The PermissionFlag required to read a person's info varies depending on the primary role.
         * This is often handled using an SQL CASE statement with the subject being the PersonRoleEnum
         * flag integer.
         *
         * For example: if one wanted check the permission required where the PersonEntity was
         * already part of the SELECT statement, one could use
         * CASE(
         *   (SELECT PersonRoleEntity.prRoleEnum
         *                           FROM PersonRoleEntity
         *                          WHERE PersonRoleEntity.prPersonGuidHash = :uidNum
         *                          LIMIT 1)
         * $CASE_STATEMENT_READ_WHEN_CLAUSES_SQL
         * END
         *
         */
        const val CASE_STATEMENT_READ_WHEN_CLAUSES_SQL = """
             WHEN ${PersonRoleEnum.SITE_ADMINISTRATOR_INT} THEN ${PermissionFlags.PERSON_ADMIN_READ}
             WHEN ${PersonRoleEnum.SYSTEM_ADMINISTRATOR_INT} THEN ${PermissionFlags.PERSON_ADMIN_READ}
             WHEN ${PersonRoleEnum.TEACHER_INT} THEN ${PermissionFlags.PERSON_TEACHER_READ}
             WHEN ${PersonRoleEnum.STUDENT_INT} THEN ${PermissionFlags.PERSON_STUDENT_READ}
             WHEN ${PersonRoleEnum.PARENT_INT} THEN ${PermissionFlags.PERSON_PARENT_READ}
              WHEN ${PersonRoleEnum.SHARED_SCHOOL_DEVICE_INT} THEN ${PermissionFlags.PERSON_STUDENT_READ}
             ELSE ${Long.MAX_VALUE}
        """


        /**
         * When expression that will evaluate to the permission flag required to read PersonEntity
         * based on the PersonEntity's primary role.
         */
        const val PERMISSION_REQUIRED_TO_READ_PERSON_EXPR = """
            CASE(SELECT PersonRoleEntity.prRoleEnum
                   FROM PersonRoleEntity
                  WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash
                  LIMIT 1)                    
                 $CASE_STATEMENT_READ_WHEN_CLAUSES_SQL
            END     
        """



        /**
         * Find ClassPermissionsEntity that are available to the authenticatedPersonUidNum
         * including the permissions granted to the enrollment roles that the authenticated person
         * has, and, if the authenticated person is a parent, the roles that their children have,
         * such that a parent can see what their children can see.
         */
        const val AUTHENTICATED_PERSON_CLASS_PERMISSIONS = """
            AuthenticatedPersonClassPermissions AS (
                SELECT ClassPermissionEntity.*
                  FROM ClassPermissionEntity
                 WHERE (ClassPermissionEntity.cpeToEnrollmentRole, ClassPermissionEntity.cpeClassUidNum) IN 
                       (SELECT EnrollmentEntity.eRole, EnrollmentEntity.eClassUidNum
                          FROM EnrollmentEntity
                         WHERE EnrollmentEntity.ePersonUidNum IN 
                               ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL)
                           AND EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT})
            )
        """


        /**
         * Where clause checking if the authenticated person has read permission for a PersonEntity
         * in the select clause.
         */
        const val AUTHENTICATED_USER_PERSON_READ_PERMISSION_WHERE_CLAUSE_SQL = """
                PersonEntity.pGuidHash = :authenticatedPersonUidNum
             OR PersonEntity.pGuidHash IN 
                (SELECT PersonRelatedPersonEntity.prpOtherPersonUidNum
                   FROM PersonRelatedPersonEntity
                  WHERE PersonRelatedPersonEntity.prpPersonUidNum = :authenticatedPersonUidNum)
             OR EXISTS(
                    SELECT 1
                      FROM SchoolPermissionGrantEntity
                     WHERE SchoolPermissionGrantEntity.spgToRole IN 
                           (SELECT PersonRoleEntity.prRoleEnum
                              FROM PersonRoleEntity
                             WHERE PersonRoleEntity.prPersonGuidHash IN 
                                   ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL))
                               AND (SchoolPermissionGrantEntity.spgPermissions & ($PERMISSION_REQUIRED_TO_READ_PERSON_EXPR)) > 0)
             OR EXISTS(
                    SELECT 1
                      FROM AuthenticatedPersonClassPermissions
                     WHERE AuthenticatedPersonClassPermissions.cpeClassUidNum IN 
                           (SELECT EnrollmentEntity.eClassUidNum
                              FROM EnrollmentEntity
                             WHERE EnrollmentEntity.ePersonUidNum = PersonEntity.pGuidHash)
                       AND (AuthenticatedPersonClassPermissions.cpePermissions & ($PERMISSION_REQUIRED_TO_READ_PERSON_EXPR)) > 0)
        """


        /**
         * This CTE expression is shared between all functions that return a list. It handles the
         * includeRelated parameter efficiently. includeRelated is required by PersonDetail/Edit
         * to load related family members _and_ the ClassDetail screen to load related family
         * members for pending enrollees where applicable.
         */
        const val LIST_PERSONS_CTES_SQL = """
            Persons(uidNum) AS (
                   SELECT PersonEntity.pGuidHash 
                     FROM PersonEntity
                    WHERE PersonEntity.pStored > :since 
                      AND (:guidHash = 0 OR PersonEntity.pGuidHash = :guidHash)
                      AND (:inClazzGuidHash = 0 OR
                           EXISTS(
                                SELECT EnrollmentEntity.eUid
                                  FROM EnrollmentEntity
                                 WHERE EnrollmentEntity.ePersonUidNum = PersonEntity.pGuidHash
                                   AND EnrollmentEntity.eClassUidNum = :inClazzGuidHash
                                   AND (:inClazzRoleFlag = 0 OR EnrollmentEntity.eRole = :inClazzRoleFlag)
                                   AND ((:includeDeleted OR :inClassOnDayInUtcMs = 0) 
                                        OR (     (:inClassOnDayInUtcMs >= COALESCE(EnrollmentEntity.eBeginDate, 0))
                                            AND ((:inClassOnDayInUtcMs - ${TimeConstants.DAY_IN_MILLIS - 1}) < COALESCE(EnrollmentEntity.eEndDate, ${Long.MAX_VALUE}))
                                            AND (:timeNow <= COALESCE(EnrollmentEntity.eRemovedAt, ${Long.MAX_VALUE}))
                                            AND EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT} ))         
                           ) 
                          ) 
                          -- Begin permission NOTE check should add permissions granted to children for parent
                      AND ($AUTHENTICATED_USER_PERSON_READ_PERMISSION_WHERE_CLAUSE_SQL)
                      AND (:filterByName IS NULL 
                           OR (PersonEntity.pGivenName || ' ' || PersonEntity.pFamilyName) LIKE ('%' || :filterByName || '%'))
                      AND (:filterByPersonRole = 0 OR :filterByPersonRole IN 
                           (SELECT PersonRoleEntity.prRoleEnum
                              FROM PersonRoleEntity
                             WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash))
                      AND (:filterByPersonStatus = 0 OR PersonEntity.pStatus = :filterByPersonStatus)
                      AND (:includeDeleted OR PersonEntity.pStatus != ${PersonStatusEnum.TO_BE_DELETED_INT}) 
                      AND (:excludeSharedSchoolDevice = 0 OR NOT EXISTS ( 
                       SELECT 1
                         FROM PersonRoleEntity
                        WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash
                          AND PersonRoleEntity.prRoleEnum = ${PersonRoleEnum.SHARED_SCHOOL_DEVICE_INT}
                  ))              
            ),
                
            RelatedPersons(uidNum) AS (
                SELECT PersonEntity.pGuidHash
                  FROM PersonEntity
                 WHERE :includeRelated 
                   AND PersonEntity.pGuidHash IN(
                        SELECT DISTINCT PersonRelatedPersonEntity.prpOtherPersonUidNum
                          FROM PersonRelatedPersonEntity
                         WHERE PersonRelatedPersonEntity.prpPersonUidNum IN(
                               SELECT Persons.uidNum
                                 FROM Persons)
                       )
                   AND ($AUTHENTICATED_USER_PERSON_READ_PERMISSION_WHERE_CLAUSE_SQL)    
                    AND (:excludeSharedSchoolDevice = 0 OR NOT EXISTS (  
                    SELECT 1
                      FROM PersonRoleEntity
                     WHERE PersonRoleEntity.prPersonGuidHash = PersonEntity.pGuidHash
                       AND PersonRoleEntity.prRoleEnum = ${PersonRoleEnum.SHARED_SCHOOL_DEVICE_INT}
               ))   
            ),
                
            AllPersons(uidNum) AS (
                SELECT uidNum 
                  FROM Persons
                 UNION
                 SELECT uidNum 
                   FROM RelatedPersons
            )
        """

        const val SELECT_PERMISSION_AND_LAST_MODIFIED_SQL = """
            WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL,
                 $AUTHENTICATED_PERSON_CLASS_PERMISSIONS,
                 RequiredPermission(flag) AS (
                 SELECT CASE(
                          SELECT COALESCE(
                                 (SELECT PersonRoleEntity.prRoleEnum
                                    FROM PersonRoleEntity
                                   WHERE PersonRoleEntity.prPersonGuidHash = :personUidNum
                                   LIMIT 1), :knownRoleFlag)
                          )
                          WHEN ${PersonRoleEnum.SITE_ADMINISTRATOR_INT} THEN :roleAdminPermissionRequired
                          WHEN ${PersonRoleEnum.SYSTEM_ADMINISTRATOR_INT} THEN :roleAdminPermissionRequired
                          WHEN ${PersonRoleEnum.TEACHER_INT} THEN :roleTeacherPermissionRequired
                          WHEN ${PersonRoleEnum.STUDENT_INT} THEN :roleStudentPermissionRequired
                          WHEN ${PersonRoleEnum.PARENT_INT} THEN :roleParentPermissionRequired
                          WHEN ${PersonRoleEnum.SHARED_SCHOOL_DEVICE_INT} THEN :roleSharedDevicePermissionRequired
                          ELSE ${Long.MAX_VALUE}
                        END
                 )
            
          SELECT :personUidNum AS uidNum,
                   (SELECT PersonEntity.pLastModified
                      FROM PersonEntity
                     WHERE PersonEntity.pGuidHash = :personUidNum) AS lastModified,
                   (    :personUidNum IN 
                        ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL)
                     OR EXISTS(
                            SELECT 1
                              FROM SchoolPermissionGrantEntity
                             WHERE SchoolPermissionGrantEntity.spgToRole IN 
                                   (SELECT PersonRoleEntity.prRoleEnum
                                      FROM PersonRoleEntity
                                     WHERE PersonRoleEntity.prPersonGuidHash IN 
                                           ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL))
                                       AND (SchoolPermissionGrantEntity.spgPermissions & (SELECT flag FROM RequiredPermission)) > 0)
                     OR EXISTS(
                            SELECT 1
                              FROM AuthenticatedPersonClassPermissions
                             WHERE AuthenticatedPersonClassPermissions.cpeClassUidNum IN 
                                   (SELECT EnrollmentEntity.eClassUidNum
                                      FROM EnrollmentEntity
                                     WHERE EnrollmentEntity.ePersonUidNum = :personUidNum)
                               AND (AuthenticatedPersonClassPermissions.cpePermissions & (SELECT flag FROM RequiredPermission)) > 0)
                   ) AS hasPermission
        """


    }
}