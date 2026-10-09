package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datalayer.db.school.entities.AuthTokenEntity

@Dao
interface AuthTokenEntityDao {

    @Insert
    suspend fun insert(token: AuthTokenEntity)

    @Query("""
        SELECT AuthTokenEntity.*
          FROM AuthTokenEntity
         WHERE AuthTokenEntity.atToken = :token
           AND :timeNow BETWEEN AuthTokenEntity.atTimeCreated 
                            AND (AuthTokenEntity.atTimeCreated + (AuthTokenEntity.atTtl * 1000))
    """)
    suspend fun findByToken(
        token: String,
        timeNow: Long,
    ): AuthTokenEntity?


}