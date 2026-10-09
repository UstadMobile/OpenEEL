package world.respect.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datalayer.db.school.opds.entities.OpdsGroupEntity

@Dao
abstract class OpdsGroupEntityDao {

    @Query("""
        SELECT OpdsGroupEntity.*
          FROM OpdsGroupEntity
         WHERE OpdsGroupEntity.ogeOfeUid = :feedUid
    """)
    abstract suspend fun findByFeedUid(feedUid: Long): List<OpdsGroupEntity>

    @Query("""
        DELETE FROM OpdsGroupEntity 
         WHERE OpdsGroupEntity.ogeOfeUid = :feedUid
    """)
    abstract suspend fun deleteByFeedUid(feedUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<OpdsGroupEntity>)

}