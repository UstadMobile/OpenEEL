package world.respect.datalayer.db.school.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.entities.AssignmentLearningResourceRefEntity

@Dao
interface AssignmentLearningResourceRefEntityDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entities: List<AssignmentLearningResourceRefEntity>)

    @Query("""
        DELETE FROM AssignmentLearningResourceRefEntity
         WHERE alrrAeUidNum = :uidNum
    """)
    suspend fun deleteByAssignmentUidNum(uidNum: Long)

}