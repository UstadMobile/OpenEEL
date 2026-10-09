package world.respect.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.entities.PersonPasskeyEntity

@Dao
abstract class PersonPasskeyEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsertAsync(
        personPasskey: List<PersonPasskeyEntity>
    )

    @Query(
        """
        SELECT * 
          FROM PersonPasskeyEntity
         WHERE isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
           AND ppPersonUidNum = :uid
    """
    )
    abstract suspend fun getAllActivePasskeysList(uid: Long): List<PersonPasskeyEntity>

    @Query(
        """
        SELECT *
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppCredentialId = :id
           AND PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
    """
    )
    abstract suspend fun findPersonPasskeyFromClientDataJson(
        id: String,
    ): PersonPasskeyEntity?

    @Query(
        """
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUidNum = :uid
           AND PersonPasskeyEntity.ppCredentialId = :credentialId
           AND PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED}
    """
    )
    abstract suspend fun findByPersonUidAndCredentialId(
        uid: Long,
        credentialId: String,
    ): PersonPasskeyEntity?


    @Query(
        """
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUidNum = :personGuidNumber
           AND (:includeRevoked = 1 
                OR PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED})
    """
    )
    abstract suspend fun findAll(
        personGuidNumber: Long,
        includeRevoked: Int,
    ): List<PersonPasskeyEntity>

    @Query(
        """
        SELECT PersonPasskeyEntity.*
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUidNum = :personGuidNumber
           AND (:includeRevoked = 1 
                OR PersonPasskeyEntity.isRevoked = ${PersonPasskeyEntity.NOT_REVOKED})
    """
    )
    abstract fun findAllAsFlow(
        personGuidNumber: Long,
        includeRevoked: Int,
    ): Flow<List<PersonPasskeyEntity>>

    @Query(
        """
        SELECT PersonPasskeyEntity.ppLastModified
          FROM PersonPasskeyEntity
         WHERE PersonPasskeyEntity.ppPersonUidNum = :personUidNum
           AND PersonPasskeyEntity.ppCredentialId = :passKeyId
    """
    )
    abstract suspend fun getLastModifiedByPersonUidAndKeyId(
        personUidNum: Long,
        passKeyId: String,
    ): Long?


}
