package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.daos.PersonEntityDao.Companion.SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL
import world.respect.datalayer.db.school.entities.ClassEntity
import world.respect.datalayer.db.school.entities.ClassEntityWithPermissions
import world.respect.datalayer.db.school.entities.LastModifiedAndPermission
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.StatusEnum

@Dao
interface ClassEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(classEntity: ClassEntity)

    @Transaction
    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cGuidHash = :guidHash
    """)
    fun findByGuidHashAsFlow(guidHash: Long): Flow<ClassEntityWithPermissions?>

    @Query("""
        SELECT * 
         FROM ClassEntity
        WHERE ClassEntity.cGuidHash = :guidHash
    """)
    suspend fun findByGuid(guidHash: Long): ClassEntityWithPermissions?


    @Query("""
        SELECT ClassEntity.cLastModified
          FROM ClassEntity
         WHERE ClassEntity.cGuidHash = :uidNum 
    """)
    suspend fun getLastModifiedByGuid(
        uidNum: Long
    ): Long?

    @Query(LIST_SQL)
    @Transaction
    fun listAsPagingSource(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
        requiredPermission: Long = PermissionFlags.CLASS_READ,
    ): PagingSource<Int, ClassEntityWithPermissions>


    @Query(
        """
        SELECT * 
          FROM ClassEntity 
        WHERE cTeacherInviteGuid = :inviteCode
    """
    )
    suspend fun findByTeacherInviteCode(inviteCode: String): ClassEntity?

    @Query(
        """
        SELECT * 
          FROM ClassEntity 
        WHERE cStudentInviteGuid = :inviteCode
    """
    )
    suspend fun findByStudentInviteCode(inviteCode: String): ClassEntity?


    @Query(LIST_SQL)
    @Transaction
    suspend fun list(
        authenticatedPersonUidNum: Long,
        since: Long = 0,
        guidHash: Long = 0,
        code: String? = null,
        requiredPermission: Long = PermissionFlags.CLASS_READ
    ): List<ClassEntityWithPermissions>

    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cGuidHash in (:uids) 
    """)
    suspend fun findByUidList(uids: List<Long>) : List<ClassEntityWithPermissions>

    @Query("""
        SELECT ClassEntity.*
          FROM ClassEntity
         WHERE ClassEntity.cStudentInviteGuid = :code
            OR ClassEntity.cTeacherInviteGuid = :code 
    """)
    suspend fun findByInviteCode(code: String): List<ClassEntityWithPermissions>

    @Query("""
          WITH ${PersonEntityDao.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL},
               $AUTHENTICATED_USER_ENROLLMENTS_CTE_SQL
        SELECT :classUidNum AS uidNum,
               (SELECT ClassEntity.cLastModified
                  FROM ClassEntity
                 WHERE ClassEntity.cGuidHash = :classUidNum) AS lastModified,
                 
               -- Begin permission check - slightly modified version of CLASS_PERMISSION_CHECK_SQL
               -- that uses classUidNum param instead of ClassEntity.cGuidHash. This way it also 
               -- works when checking permission to add a new class that does not yet exist
               (EXISTS(
                   SELECT 1
                     FROM SchoolPermissionGrantEntity
                    WHERE SchoolPermissionGrantEntity.spgToRole IN (
                          SELECT PersonRoleEntity.prRoleEnum
                            FROM PersonRoleEntity
                           WHERE PersonRoleEntity.prPersonGuidHash IN 
                                 ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL)) 
                      AND (SchoolPermissionGrantEntity.spgPermissions & :requiredPermission) = :requiredPermission
                      AND SchoolPermissionGrantEntity.spgStatusEnum = ${StatusEnum.ACTIVE_INT})
                OR EXISTS(
                   SELECT 1
                     FROM ClassPermissionEntity
                    WHERE ClassPermissionEntity.cpeClassUidNum = :classUidNum
                      AND ClassPermissionEntity.cpeToEnrollmentRole IN 
                          (SELECT AuthenticatedUserEnrollments.eRole
                             FROM AuthenticatedUserEnrollments
                            WHERE AuthenticatedUserEnrollments.eClassUidNum = :classUidNum)
                      AND (ClassPermissionEntity.cpePermissions & :requiredPermission) = :requiredPermission)
                ) AS hasPermission
    """)
    suspend fun getLastModifiedAndHasPermission(
        authenticatedPersonUidNum: Long,
        classUidNum: Long,
        requiredPermission: Long,
    ): LastModifiedAndPermission

    companion object {

        /**
         * CTE SQL to get a list of enrollments for the authenticated user (needed for permission
         * checks).
         */
        const val AUTHENTICATED_USER_ENROLLMENTS_CTE_SQL = """
        AuthenticatedUserEnrollments AS (
             SELECT EnrollmentEntity.*
               FROM EnrollmentEntity
              WHERE EnrollmentEntity.ePersonUidNum IN
                    (SELECT AuthenticatedPermissionPersonUids.uidNum
                       FROM AuthenticatedPermissionPersonUids)
                AND EnrollmentEntity.eStatus = ${StatusEnum.ACTIVE_INT}
        )
        """

        /**
         * Permission check SQL to see if the authenticated user has the required permission for
         * an existing class (where Class is already in the query as ClassEntity).
         *
         * Parameters:
         *  :authenticatedPersonUidNum - the uid number for the authenticated user
         *  :requiredPermission - the PermissionFlags constant for the permission required
         *
         */
        const val CLASS_PERMISSION_CHECK_SQL = """
                EXISTS(
                   SELECT 1
                     FROM SchoolPermissionGrantEntity
                    WHERE SchoolPermissionGrantEntity.spgToRole IN (
                          SELECT PersonRoleEntity.prRoleEnum
                            FROM PersonRoleEntity
                           WHERE PersonRoleEntity.prPersonGuidHash IN
                                 ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL)) 
                      AND (SchoolPermissionGrantEntity.spgPermissions & :requiredPermission) = :requiredPermission
                      AND SchoolPermissionGrantEntity.spgStatusEnum = ${StatusEnum.ACTIVE_INT})
                OR EXISTS(
                   SELECT 1
                     FROM ClassPermissionEntity
                    WHERE ClassPermissionEntity.cpeClassUidNum = ClassEntity.cGuidHash
                      AND ClassPermissionEntity.cpeToEnrollmentRole IN 
                          (SELECT AuthenticatedUserEnrollments.eRole
                             FROM AuthenticatedUserEnrollments
                            WHERE AuthenticatedUserEnrollments.eClassUidNum = ClassEntity.cGuidHash)
                      AND (ClassPermissionEntity.cpePermissions & :requiredPermission) = :requiredPermission)
        """

        const val LIST_SQL = """
        WITH ${PersonEntityDao.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL},
              $AUTHENTICATED_USER_ENROLLMENTS_CTE_SQL
            
       SELECT ClassEntity.* 
         FROM ClassEntity
        WHERE ClassEntity.cStored > :since 
          AND (:guidHash = 0 OR ClassEntity.cGuidHash = :guidHash)
          AND ($CLASS_PERMISSION_CHECK_SQL)
          
          AND (:code IS NULL 
                OR ClassEntity.cStudentInviteGuid = :code
                OR ClassEntity.cTeacherInviteGuid = :code)
     ORDER BY ClassEntity.cTitle
        """

    }
}