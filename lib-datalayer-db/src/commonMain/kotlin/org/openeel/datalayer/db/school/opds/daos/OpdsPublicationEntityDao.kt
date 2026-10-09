package world.respect.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.db.school.opds.entities.OpdsPublicationEntity
import world.respect.datalayer.db.shared.LastModifiedAndETagDb

@Dao
abstract class OpdsPublicationEntityDao {

    @Query("""
        SELECT OpdsPublicationEntity.*
          FROM OpdsPublicationEntity
         WHERE opeOfeUid = :feedUid 
    """)
    abstract suspend fun findByFeedUid(feedUid: Long): List<OpdsPublicationEntity>

    @Query("""
        DELETE FROM OpdsPublicationEntity 
         WHERE opeOfeUid = :feedUid
    """)
    abstract suspend fun deleteAllByFeedUid(feedUid: Long)

    @Query("""
        DELETE FROM OpdsPublicationEntity 
         WHERE opeUid = :pubUid
    """)
    abstract suspend fun deleteByUid(pubUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<OpdsPublicationEntity>)

    @Query("""
        SELECT OpdsPublicationEntity.opeUid
          FROM OpdsPublicationEntity
         WHERE OpdsPublicationEntity.opeUrlHash = :urlHash
    """)
    abstract suspend fun getUidByUrlHash(urlHash: Long): Long

    @Query("""
        SELECT OpdsPublicationEntity.*
          FROM OpdsPublicationEntity
         WHERE OpdsPublicationEntity.opeUrlHash = :urlHash
    """)
    abstract fun findByUrlHashAsFlow(urlHash: Long): Flow<OpdsPublicationEntity?>

    @Query("""
        SELECT OpdsPublicationEntity.*
          FROM OpdsPublicationEntity
         WHERE OpdsPublicationEntity.opeUrlHash = :urlHash
    """)
    abstract suspend fun findByUrlHash(urlHash: Long): OpdsPublicationEntity?

    @Query("""
        SELECT OpdsPublicationEntity.opeLastModified AS lastModified,
               OpdsPublicationEntity.opeEtag AS etag
          FROM OpdsPublicationEntity
         WHERE OpdsPublicationEntity.opeUrlHash = :urlHash 
    """)
    abstract suspend fun getLastModifiedAndETag(urlHash: Long): LastModifiedAndETagDb?

    companion object {

        const val PUBLICATION_UIDS_FOR_FEED_UID_CTE = """
            FeedPublicationUids(publicationUid) AS(
             SELECT OpdsPublicationEntity.opeUid
               FROM OpdsPublicationEntity
              WHERE OpdsPublicationEntity.opeOfeUid = :feedUid 
        )
        """

    }
}