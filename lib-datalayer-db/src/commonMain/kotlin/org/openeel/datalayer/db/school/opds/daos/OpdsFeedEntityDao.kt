package org.openeel.datalayer.db.school.opds.daos

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.db.school.opds.OpdsParentType
import org.openeel.datalayer.db.school.opds.entities.OpdsFeedEntity
import org.openeel.datalayer.db.school.opds.entities.ReadiumLinkEntity
import org.openeel.datalayer.db.shared.LastModifiedAndETagDb
import org.openeel.datalayer.db.shared.entities.LangMapEntity
import org.openeel.datalayer.school.model.composites.OpdsSearchResult

@Dao
abstract class OpdsFeedEntityDao {

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

    /**
     * Search all cached publications and navigation links (from all feeds stored locally).
     *
     * Publications are matched on any title (LangMap) and the selected filters. The same
     * publication can be stored more than once (e.g. when it is in more than one feed), so matches
     * are de-duplicated by identifier (or URL). Navigation links are matched on title and
     * de-duplicated by feed and href.
     *
     * @param searchText text to search for in titles. When empty, all titles match.
     * @param filterByLanguage when true, only publications with a language in [languages] match.
     * @param filterBySubject when true, only publications with a subject name in [subjects] match.
     * @param filterByType when true, only publications with a schema.org type in [types] match.
     * @param includePublications when false, publications are excluded from the results.
     * @param includeNavigation when false, navigation links are excluded from the results.
     */
    //json_each is a built-in SQLite function, but the IDE Room SQL checker does not recognise it
    //noinspection AndroidUnresolvedRoomSqlReference
    @Query("""
        WITH MatchingPublication(opeUid) AS (
             SELECT MIN(OpdsPublicationEntity.opeUid)
               FROM OpdsPublicationEntity
              WHERE :includePublications
                AND (:searchText = ''
                     OR EXISTS(
                        SELECT 1
                          FROM LangMapEntity AS PublicationTitle
                         WHERE PublicationTitle.lmeTopParentType = :publicationLangMapParentType
                           AND PublicationTitle.lmeTopParentUid1 = OpdsPublicationEntity.opeUid
                           AND PublicationTitle.lmePropType = :publicationTitlePropType
                           AND PublicationTitle.lmeValue LIKE ('%' || :searchText || '%')))
                AND (NOT :filterByLanguage
                     OR EXISTS(
                        SELECT 1
                          FROM json_each(OpdsPublicationEntity.opeMdLanguage) AS PublicationLanguage
                         WHERE PublicationLanguage.value IN (:languages)))
                AND (NOT :filterBySubject
                     OR EXISTS(
                        SELECT 1
                          FROM ReadiumSubjectEntity
                         WHERE ReadiumSubjectEntity.rseTopParentType = :publicationParentType
                           AND ReadiumSubjectEntity.rseTopParentUid = OpdsPublicationEntity.opeUid
                           AND $SUBJECT_NAME_SQL IN (:subjects)))
                AND (NOT :filterByType
                     OR OpdsPublicationEntity.opeMdType IN (:types))
           GROUP BY COALESCE(OpdsPublicationEntity.opeMdIdentifier,
                             OpdsPublicationEntity.opeUrl,
                             OpdsPublicationEntity.opeUid)
        ),
        MatchingNavigationLink(rleId) AS (
             SELECT MIN(ReadiumLinkEntity.rleId)
               FROM ReadiumLinkEntity
              WHERE :includeNavigation
                AND ReadiumLinkEntity.rleOpdsParentType = :feedParentType
                AND ReadiumLinkEntity.rlePropType IN (:feedNavigationPropType, :groupNavigationPropType)
                AND ReadiumLinkEntity.rleTitle LIKE ('%' || :searchText || '%')
           GROUP BY ReadiumLinkEntity.rleOpdsParentUid, ReadiumLinkEntity.rleHref
        )
        
        SELECT COALESCE(
                   (SELECT PublicationTitle.lmeValue
                      FROM LangMapEntity AS PublicationTitle
                     WHERE PublicationTitle.lmeTopParentType = :publicationLangMapParentType
                       AND PublicationTitle.lmeTopParentUid1 = OpdsPublicationEntity.opeUid
                       AND PublicationTitle.lmePropType = :publicationTitlePropType
                  ORDER BY PublicationTitle.lmeId
                     LIMIT 1), '') AS title,
               1 AS isPublication,
               OpdsPublicationEntity.opeUrl AS url,
               (SELECT PublicationSelfLink.rleHref
                  FROM ReadiumLinkEntity AS PublicationSelfLink
                 WHERE PublicationSelfLink.rleOpdsParentType = :publicationParentType
                   AND PublicationSelfLink.rleOpdsParentUid = OpdsPublicationEntity.opeUid
                   AND PublicationSelfLink.rlePropType = :publicationLinkPropType
                   AND PublicationSelfLink.rleRel LIKE '%"self"%'
              ORDER BY PublicationSelfLink.rleIndex
                 LIMIT 1) AS href,
               PublicationFeed.ofeUrl AS feedUrl,
               OpdsPublicationEntity.opeMdIdentifier AS identifier,
               (SELECT PublicationImage.rleHref
                  FROM ReadiumLinkEntity AS PublicationImage
                 WHERE PublicationImage.rleOpdsParentType = :publicationParentType
                   AND PublicationImage.rleOpdsParentUid = OpdsPublicationEntity.opeUid
                   AND PublicationImage.rlePropType = :publicationImagePropType
              ORDER BY PublicationImage.rleIndex
                 LIMIT 1) AS iconHref
          FROM MatchingPublication
               JOIN OpdsPublicationEntity
                    ON OpdsPublicationEntity.opeUid = MatchingPublication.opeUid
               LEFT JOIN OpdsFeedEntity AS PublicationFeed
                    ON PublicationFeed.ofeUid = OpdsPublicationEntity.opeOfeUid
                    
        UNION ALL
        
        SELECT ReadiumLinkEntity.rleTitle AS title,
               0 AS isPublication,
               NULL AS url,
               ReadiumLinkEntity.rleHref AS href,
               NavigationLinkFeed.ofeUrl AS feedUrl,
               NULL AS identifier,
               (SELECT NavigationIcon.rleHref
                  FROM ReadiumLinkEntity AS NavigationIcon
                 WHERE NavigationIcon.rlePropType = :linkAlternatePropType
                   AND NavigationIcon.rlePropFk = ReadiumLinkEntity.rleId
                   AND NavigationIcon.rleRel LIKE '%"icon"%'
              ORDER BY NavigationIcon.rleIndex
                 LIMIT 1) AS iconHref
          FROM MatchingNavigationLink
               JOIN ReadiumLinkEntity
                    ON ReadiumLinkEntity.rleId = MatchingNavigationLink.rleId
               JOIN OpdsFeedEntity AS NavigationLinkFeed
                    ON NavigationLinkFeed.ofeUid = ReadiumLinkEntity.rleOpdsParentUid
                    
      ORDER BY title
    """)
    abstract fun searchAsPagingSource(
        searchText: String,
        filterByLanguage: Boolean,
        languages: List<String>,
        filterBySubject: Boolean,
        subjects: List<String>,
        filterByType: Boolean,
        types: List<String>,
        includePublications: Boolean,
        includeNavigation: Boolean,
        publicationParentType: OpdsParentType = OpdsParentType.OPDS_PUBLICATION,
        publicationLangMapParentType: LangMapEntity.TopParentType =
            LangMapEntity.TopParentType.OPDS_PUBLICATION,
        publicationTitlePropType: LangMapEntity.PropType = LangMapEntity.PropType.OPDS_PUB_TITLE,
        subjectNamePropType: LangMapEntity.PropType = LangMapEntity.PropType.READIUM_SUBJECT_NAME,
        publicationLinkPropType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.OPDS_PUB_LINKS,
        publicationImagePropType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.OPDS_PUB_IMAGES,
        linkAlternatePropType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.LINK_ALTERNATE,
        feedParentType: OpdsParentType = OpdsParentType.OPDS_FEED,
        feedNavigationPropType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.OPDS_FEED_NAVIGATION,
        groupNavigationPropType: ReadiumLinkEntity.PropertyType =
            ReadiumLinkEntity.PropertyType.OPDS_GROUP_NAVIGATION,
    ): PagingSource<Int, OpdsSearchResult>

    /**
     * Get the distinct languages used by cached publications (for search filter options).
     */
    //noinspection AndroidUnresolvedRoomSqlReference
    @Query("""
        SELECT DISTINCT PublicationLanguage.value
          FROM OpdsPublicationEntity
               JOIN json_each(OpdsPublicationEntity.opeMdLanguage) AS PublicationLanguage
      ORDER BY PublicationLanguage.value
    """)
    abstract fun findPublicationLanguagesAsFlow(): Flow<List<String>>

    /**
     * Get the distinct subject names used by cached publications (for search filter options).
     */
    @Query("""
        SELECT DISTINCT $SUBJECT_NAME_SQL AS subjectName
          FROM ReadiumSubjectEntity
         WHERE ReadiumSubjectEntity.rseTopParentType = :publicationParentType
           AND subjectName IS NOT NULL
      ORDER BY subjectName
    """)
    abstract fun findPublicationSubjectsAsFlow(
        publicationParentType: OpdsParentType = OpdsParentType.OPDS_PUBLICATION,
        subjectNamePropType: LangMapEntity.PropType = LangMapEntity.PropType.READIUM_SUBJECT_NAME,
    ): Flow<List<String>>

    companion object {

        /**
         * The name of a ReadiumSubjectEntity: a subject is either a simple string value or an
         * object with a name LangMap (in which case the first name is used). Requires the
         * :subjectNamePropType parameter.
         */
        private const val SUBJECT_NAME_SQL = """
            COALESCE(ReadiumSubjectEntity.rseStringValue,
                     (SELECT SubjectName.lmeValue
                        FROM LangMapEntity AS SubjectName
                       WHERE SubjectName.lmePropType = :subjectNamePropType
                         AND SubjectName.lmePropFk = ReadiumSubjectEntity.rseUid
                    ORDER BY SubjectName.lmeId
                       LIMIT 1))
        """

    }

}