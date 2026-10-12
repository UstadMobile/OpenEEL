package org.openeel.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.db.school.opds.entities.OpdsPublicationEntity
import org.openeel.datalayer.db.shared.LastModifiedAndETagDb
import org.openeel.datalayer.db.shared.entities.LangMapEntity

@Dao
abstract class OpdsPublicationEntityDao {

    @Query("""
        SELECT DISTINCT p.opeUrl
          FROM OpdsPublicationEntity AS p
         WHERE p.opeOfeUid = 0
           AND p.opeUrl IS NOT NULL
           AND EXISTS (
               SELECT 1
                 FROM LangMapEntity AS t
                WHERE t.lmeTopParentUid1 = p.opeUid
                  AND t.lmeTopParentType = :titleParentType
                  AND t.lmePropType = :titlePropType
                  AND INSTR(LOWER(t.lmeValue), LOWER(:title)) > 0
           )
    """)
    abstract fun searchByTitleAsFlow(
        title: String,
        titleParentType: LangMapEntity.TopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION,
        titlePropType: LangMapEntity.PropType = LangMapEntity.PropType.OPDS_PUB_TITLE,
    ): Flow<List<Url>>

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