package org.openeel.datalayer.db.school.opds.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.db.school.opds.OpdsParentType
import org.openeel.datalayer.db.school.opds.entities.OpdsFeedEntity
import org.openeel.datalayer.db.school.opds.entities.ReadiumLinkEntity
import org.openeel.datalayer.db.shared.LastModifiedAndETagDb
import org.openeel.datalayer.db.shared.entities.LangMapEntity
import org.openeel.lib.opds.model.ext.OpdsFeedSearchMatch

@Dao
abstract class OpdsFeedEntityDao {

    @Query("""
        -- Search publication titles and return their original feed/group positions.
        SELECT COALESCE(g.ogeIndex, -1) AS groupIndex,
               p.opeIndex AS `index`, 1 AS isPublication
          FROM OpdsPublicationEntity AS p
          LEFT JOIN OpdsGroupEntity AS g ON g.ogeUid = p.opeOgeUid
          JOIN LangMapEntity AS t ON t.lmeTopParentUid1 = p.opeUid
         WHERE p.opeOfeUid = :feedUid
           AND t.lmeTopParentType = :titleParentType
           AND t.lmePropType = :titlePropType
           AND t.lmeValue LIKE :titlePattern
        UNION
        -- Search navigation titles; isPublication distinguishes these from publications.
        SELECT COALESCE(g.ogeIndex, -1) AS groupIndex,
               l.rleIndex AS `index`, 0 AS isPublication
          FROM ReadiumLinkEntity AS l
          LEFT JOIN OpdsGroupEntity AS g
            ON g.ogeUid = l.rlePropFk AND l.rlePropType = :groupNavigationType
         WHERE l.rleOpdsParentType = :linkParentType
           AND l.rleOpdsParentUid = :feedUid
           AND l.rlePropType IN (:feedNavigationType, :groupNavigationType)
           AND l.rleTitle LIKE :titlePattern
    """)
    abstract fun searchByTitleAsFlow(
        feedUid: Long,
        titlePattern: String,
        titleParentType: LangMapEntity.TopParentType = LangMapEntity.TopParentType.OPDS_PUBLICATION,
        titlePropType: LangMapEntity.PropType = LangMapEntity.PropType.OPDS_PUB_TITLE,
        linkParentType: OpdsParentType = OpdsParentType.OPDS_FEED,
        feedNavigationType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.OPDS_FEED_NAVIGATION,
        groupNavigationType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.OPDS_GROUP_NAVIGATION,
    ): Flow<List<OpdsFeedSearchMatch>>

    @Query("""
        SELECT * 
          FROM OpdsFeedEntity 
         WHERE ofeUrlHash = :urlHash
    """)
    abstract fun findByUrlHashAsFlow(urlHash: Long): Flow<OpdsFeedEntity?>

    @Query("""
        SELECT * 
          FROM OpdsFeedEntity 
         WHERE ofeUrlHash = :urlHash
    """)
    abstract suspend fun findByUrlHash(urlHash: Long): OpdsFeedEntity?

    @Query("""
        SELECT OpdsFeedEntity.ofeLastModifiedHeader AS lastModified,
               OpdsFeedEntity.ofeEtag AS etag
          FROM OpdsFeedEntity
         WHERE OpdsFeedEntity.ofeUrlHash = :urlHash
     
    """)
    abstract suspend fun getNetworkValidationInfo(urlHash: Long): LastModifiedAndETagDb?


    @Query("""
        DELETE FROM OpdsFeedEntity 
         WHERE ofeUid = :feedUid
    """)
    abstract suspend fun deleteByFeedUid(feedUid: Long)

    @Insert
    abstract suspend fun insertList(entities: List<OpdsFeedEntity>)

    @Query(
        """
        SELECT OpdsFeedEntity.ofeLastModified AS lastModified,
               OpdsFeedEntity.ofeEtag AS etag
          FROM OpdsFeedEntity
         WHERE ofeUrlHash = :urlHash
    """
    )
    abstract suspend fun getLastModifiedAndETag(urlHash: Long): LastModifiedAndETagDb?

    @Query(
        """
        SELECT OpdsFeedEntity.*
          FROM OpdsFeedEntity
         WHERE ofeUrlHash IN (:urlHashes)
    """
    )
    abstract suspend fun findByUrlHashList(
        urlHashes: List<Long>
    ): List<OpdsFeedEntity>

}