package world.respect.datalayer.db.shared.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.opds.daos.OpdsPublicationEntityDao.Companion.PUBLICATION_UIDS_FOR_FEED_UID_CTE
import world.respect.datalayer.db.shared.entities.LangMapEntity
import world.respect.datalayer.db.shared.entities.LangMapEntity.Companion.ODPS_PUBLICATION_PARENT_ID
import world.respect.datalayer.db.shared.entities.LangMapEntity.Companion.OPDS_FEED_PARENT_ID

@Dao
abstract class LangMapEntityDao {

    @Insert
    abstract suspend fun insertAsync(entity: List<LangMapEntity>)

    @Query(
        """
        DELETE FROM LangMapEntity
         WHERE LangMapEntity.lmeTopParentType = :lmeTopParentType
           AND LangMapEntity.lmeTopParentUid1 = :lmeEntityUid1
           AND LangMapEntity.lmeTopParentUid2 = :lmeEntityUid2
    """
    )
    abstract suspend fun deleteByTableAndTopParentType(
        lmeTopParentType: Int,
        lmeEntityUid1: Long,
        lmeEntityUid2: Long = 0,
    )

    @Query(
        """
        SELECT * 
          FROM LangMapEntity
         WHERE LangMapEntity.lmeTopParentType = :lmeTopParentType 
    """
    )
    abstract fun selectAllByTopParentType(
        lmeTopParentType: Int,
    ): Flow<List<LangMapEntity>>

    @Query(
        """
        SELECT * 
          FROM LangMapEntity
         WHERE LangMapEntity.lmeTopParentType = :lmeTopParentType
           AND LangMapEntity.lmeTopParentUid1 = :lmeEntityUid1
           AND LangMapEntity.lmeTopParentUid2 = :lmeEntityUid2
    """
    )
    abstract suspend fun selectAllByTableAndEntityId(
        lmeTopParentType: Int,
        lmeEntityUid1: Long,
        lmeEntityUid2: Long,
    ): List<LangMapEntity>


    @Query(
        """
        SELECT * 
          FROM LangMapEntity
         WHERE LangMapEntity.lmeValue = :value
           AND LangMapEntity.lmeTopParentType = :topParentType
           AND LangMapEntity.lmePropType = :propType
    """
    )
    abstract suspend fun searchByLmeValue(
        value: String,
        topParentType: Int,
        propType: Int,
    ): List<LangMapEntity>


    @Query(
        """
        SELECT * 
          FROM LangMapEntity
         WHERE LangMapEntity.lmeTopParentUid1 = :lmeTopParentUid1
    """
    )
    abstract suspend fun findByTopParentUid1(
        lmeTopParentUid1: String,
    ): List<LangMapEntity>

    @Query(
        """
        SELECT * 
          FROM LangMapEntity
         WHERE LangMapEntity.lmeTopParentType = :lmeTopParentTypeId
           AND LangMapEntity.lmeTopParentUid1 = :lmeEntityUid1
           AND LangMapEntity.lmeTopParentUid2 = :lmeEntityUid2
    """
    )
    abstract fun selectAllByTableAndEntityIdAsFlow(
        lmeTopParentTypeId: Int,
        lmeEntityUid1: Long,
        lmeEntityUid2: Long,
    ): Flow<List<LangMapEntity>>


    @Query("""
        WITH $PUBLICATION_UIDS_FOR_FEED_UID_CTE
         
        SELECT LangMapEntity.*
          FROM LangMapEntity
         WHERE $LANG_MAPS_FOR_FEEDUID_CLAUSE
    """)
    abstract suspend fun findAllByFeedUid(feedUid: Long): List<LangMapEntity>

    @Query("""
        WITH $PUBLICATION_UIDS_FOR_FEED_UID_CTE
        
      DELETE FROM LangMapEntity
       WHERE $LANG_MAPS_FOR_FEEDUID_CLAUSE
    """)
    abstract suspend fun deleteAllByFeedUid(feedUid: Long)

    companion object {

        const val LANG_MAPS_FOR_FEEDUID_CLAUSE = """
               (    LangMapEntity.lmeTopParentType = $OPDS_FEED_PARENT_ID
                AND LangMapEntity.lmeTopParentUid1 = :feedUid)
            OR (    LangMapEntity.lmeTopParentType = $ODPS_PUBLICATION_PARENT_ID
                AND LangMapEntity.lmeTopParentUid1 IN (SELECT publicationUid FROM FeedPublicationUids))    
        """


    }


}