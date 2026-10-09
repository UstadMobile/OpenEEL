package world.respect.datalayer.db.networkvalidation.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.networkvalidation.entities.NetworkValidationInfoEntity

@Dao
interface NetworkValidationInfoEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: NetworkValidationInfoEntity)

    @Query("""
        SELECT NetworkValidationInfoEntity.nviVaryHeader 
          FROM NetworkValidationInfoEntity
         WHERE NetworkValidationInfoEntity.nviUrlHash = :nviUrlHash
      ORDER BY NetworkValidationInfoEntity.nviLastChecked DESC
         LIMIT 1
    """)
    suspend fun getLatestVaryHeaderByUrlHash(nviUrlHash: Long): String?

    @Query(
        """
        SELECT NetworkValidationInfoEntity.* 
          FROM NetworkValidationInfoEntity
         WHERE NetworkValidationInfoEntity.nviUrlHash = :urlHash
           AND NetworkValidationInfoEntity.nviKey = :validationInfoKey
    """
    )
    suspend fun getValidationInfo(
        urlHash: Long,
        validationInfoKey: Long,
    ): NetworkValidationInfoEntity?


}