package org.openeel.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.openeel.datalayer.db.school.entities.PersonRoleEntity

@Dao
interface PersonRoleEntityDao {

    @Query("""
        DELETE FROM PersonRoleEntity
         WHERE prPersonGuidHash = :personGuidHash
    """)
    suspend fun deleteByPersonGuidHash(personGuidHash: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertList(personRoleEntities: List<PersonRoleEntity>)

}