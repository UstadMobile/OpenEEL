package org.openeel.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.openeel.datalayer.db.school.entities.PersonRelatedPersonEntity


@Dao
interface PersonRelatedPersonEntityDao {

    @Query("""
        DELETE FROM PersonRelatedPersonEntity
         WHERE PersonRelatedPersonEntity.prpPersonUidNum = :uidNum
    """)
    suspend fun deleteByPersonUidNum(uidNum: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(list: List<PersonRelatedPersonEntity>)

}