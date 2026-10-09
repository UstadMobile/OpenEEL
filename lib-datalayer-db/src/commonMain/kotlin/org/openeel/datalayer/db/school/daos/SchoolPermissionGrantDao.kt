package world.respect.datalayer.db.school.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.daos.PersonEntityDao.Companion.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL
import world.respect.datalayer.db.school.daos.PersonEntityDao.Companion.SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL
import world.respect.datalayer.db.school.entities.SchoolPermissionGrantEntity

@Dao
interface SchoolPermissionGrantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<SchoolPermissionGrantEntity>)

    @Query("""
          WITH $AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE (:uidNum = 0 OR SchoolPermissionGrantEntity.spgUidNum = :uidNum)
           AND (SchoolPermissionGrantEntity.spgToRole IN 
                (SELECT PersonRoleEntity.prRoleEnum
                   FROM PersonRoleEntity
                  WHERE PersonRoleEntity.prPersonGuidHash IN 
                        ($SELECT_AUTHENTICATED_PERMISSION_PERSON_UIDS_SQL)))
    """)
    suspend fun list(
        authenticatedPersonUidNum: Long,
        uidNum: Long,
    ): List<SchoolPermissionGrantEntity>

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE (:uidNum = 0 OR SchoolPermissionGrantEntity.spgUidNum = :uidNum)
    """)
    fun listAsPagingSource(
        uidNum: Long
    ): PagingSource<Int, SchoolPermissionGrantEntity>

    @Query("""
        SELECT SchoolPermissionGrantEntity.spgLastModified
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum = :uidNum
    """)
    suspend fun getLastModifiedByUidNum(uidNum: Long): Long?

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum = :uidNum
    """)
    suspend fun findByUidNum(uidNum: Long): SchoolPermissionGrantEntity?

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum = :uidNum
    """)
    fun findByUidNumAsFlow(uidNum: Long): Flow<SchoolPermissionGrantEntity?>

    @Query("""
        SELECT SchoolPermissionGrantEntity.*
          FROM SchoolPermissionGrantEntity
         WHERE SchoolPermissionGrantEntity.spgUidNum IN (:uidNums)
    """)
    suspend fun findByUidNums(uidNums: List<Long>): List<SchoolPermissionGrantEntity>

    @Query("""
        SELECT EXISTS(
               SELECT 1
                 FROM SchoolPermissionGrantEntity
                WHERE SchoolPermissionGrantEntity.spgToRole IN (
                      SELECT PersonRoleEntity.prRoleEnum
                        FROM PersonRoleEntity
                       WHERE PersonRoleEntity.prPersonGuidHash = :authenticatedPersonUidNum) 
                  AND (SchoolPermissionGrantEntity.spgPermissions & :permissionFlag) = :permissionFlag)
    """)
    suspend fun personHasPermission(
        authenticatedPersonUidNum: Long,
        permissionFlag: Long
    ): Boolean

}
