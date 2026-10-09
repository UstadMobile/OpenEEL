package world.respect.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import world.respect.datalayer.db.school.opds.OpdsParentType
import world.respect.datalayer.db.school.opds.daos.OpdsPublicationEntityDao.Companion.PUBLICATION_UIDS_FOR_FEED_UID_CTE
import world.respect.datalayer.db.school.opds.entities.ReadiumSubjectEntity

@Dao
abstract class ReadiumSubjectEntityDao {

    @Query("""
         WITH $PUBLICATION_UIDS_FOR_FEED_UID_CTE

       SELECT ReadiumSubjectEntity.*
         FROM ReadiumSubjectEntity
        WHERE $SUBJECT_ENTITIES_FOR_FEED_UID_WHERE_CLAUSE
    """)
    abstract suspend fun findAllByFeedUid(feedUid: Long): List<ReadiumSubjectEntity>

    @Query("""
        SELECT ReadiumSubjectEntity.*
          FROM ReadiumSubjectEntity
         WHERE ReadiumSubjectEntity.rseTopParentType = ${OpdsParentType.ID_PUBLICATION}
           AND ReadiumSubjectEntity.rseTopParentUid = :publicationUid
    """)
    abstract suspend fun findAllByPubUid(publicationUid: Long): List<ReadiumSubjectEntity>

    @Query("""
        WITH $PUBLICATION_UIDS_FOR_FEED_UID_CTE

       DELETE
         FROM ReadiumSubjectEntity
        WHERE $SUBJECT_ENTITIES_FOR_FEED_UID_WHERE_CLAUSE
    """)
    abstract suspend fun deleteAllByFeedUid(feedUid: Long)

    @Query("""
       DELETE
         FROM ReadiumSubjectEntity
        WHERE ReadiumSubjectEntity.rseTopParentUid = :publicationUid
          AND ReadiumSubjectEntity.rseTopParentType = ${OpdsParentType.ID_PUBLICATION}
    """)
    abstract suspend fun deleteAllByPublicationUid(publicationUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<ReadiumSubjectEntity>)

    companion object {

        const val SUBJECT_ENTITIES_FOR_FEED_UID_WHERE_CLAUSE = """
              (     ReadiumSubjectEntity.rseTopParentType = ${OpdsParentType.ID_FEED}
                AND ReadiumSubjectEntity.rseTopParentUid = :feedUid)
          OR  (     ReadiumSubjectEntity.rseTopParentType = ${OpdsParentType.ID_PUBLICATION}
                AND ReadiumSubjectEntity.rseTopParentUid IN (SELECT publicationUid FROM FeedPublicationUids))
        """

    }

}