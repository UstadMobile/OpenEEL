package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonQrBadgeEntity

@Dao
interface PersonQrBadgeEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAsyncList(list: List<PersonQrBadgeEntity>)

    @Query(LIST_SQL)
    suspend fun findAll(
        authenticatedPersonUidNum: Long,
        personGuidNum: Long,
        includeDeleted: Boolean = false,
        qrCodeUrl: String?,
    ): List<PersonQrBadgeEntity>

    @Query(
        """
           SELECT PersonQrBadgeEntity.* 
             FROM PersonQrBadgeEntity
            WHERE pqrGuid = :guid
        """
    )
    suspend fun findByGuid(guid: String): PersonQrBadgeEntity?

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrQrCodeUrl = :qrCodeUrl
           AND PersonQrBadgeEntity.pqrStatus = 1
    """
    )
    suspend fun findByQrCodeUrl(qrCodeUrl: String): PersonQrBadgeEntity?

    @Query(
        """
        SELECT PersonQrBadgeEntity.pqrLastModified
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuidNum = :uidNum
    """
    )
    suspend fun getLastModifiedByUidNum(
        uidNum: Long
    ): Long?

    @Query(
        """
        SELECT PersonQrBadgeEntity.*
          FROM PersonQrBadgeEntity
         WHERE PersonQrBadgeEntity.pqrGuidNum IN (:uids)
    """
    )
    suspend fun findByUidList(
        uids: List<Long>
    ): List<PersonQrBadgeEntity>

    @Query(LIST_SQL)
    fun findAllAsFlow(
        authenticatedPersonUidNum: Long,
        personGuidNum: Long,
        includeDeleted: Boolean = false,
        qrCodeUrl: String?,
    ): Flow<List<PersonQrBadgeEntity>>

    @Query(
        """
        SELECT * 
         FROM PersonQrBadgeEntity
        WHERE pqrGuidNum = :guidnum
          AND pqrStatus = 1
    """
    )
    fun findByGuidHashAsFlow(guidnum: Long): Flow<PersonQrBadgeEntity?>

    companion object {

        const val LIST_SQL = """
            WITH ${PersonEntityDao.AUTHENTICATED_PERMISSION_PERSON_UIDS_CTE_SQL},  
                 ${PersonEntityDao.AUTHENTICATED_PERSON_CLASS_PERMISSIONS}
                 
          SELECT PersonQrBadgeEntity.*
            FROM PersonQrBadgeEntity
                 JOIN PersonEntity 
                      ON PersonEntity.pGuidHash = PersonQrBadgeEntity.pqrGuidNum

           WHERE (:personGuidNum = 0 OR PersonQrBadgeEntity.pqrGuidNum = :personGuidNum)
             AND (:includeDeleted OR PersonQrBadgeEntity.pqrStatus = 1)        
             AND (:qrCodeUrl IS NULL OR PersonQrBadgeEntity.pqrQrCodeUrl = :qrCodeUrl)
             AND (${PersonEntityDao.AUTHENTICATED_USER_PERSON_READ_PERMISSION_WHERE_CLAUSE_SQL})    
        """

    }

}