package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.composites.XapiActivityProfileDocumentAndShaEntities
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import world.respect.datalayer.db.shared.ETagAndLastModifiedAsStrings
import world.respect.datalayer.db.shared.InstantAsTimestampString

@Dao
interface XapiActivityProfileDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiActivityProfileDocumentEntity)

    @Query(
        """
        SELECT activity_profile_document.*, 
               activity_profile_document_sha.sha1_digest AS sha1
          FROM activity_profile_document 
               JOIN activity_profile_document_sha
                    ON activity_profile_document_sha.doc_id = activity_profile_document.id
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
        """
    )
    suspend fun findByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    ): XapiActivityProfileDocumentAndShaEntities?

    @Query("""
        SELECT activity_profile_document.last_modified AS lastModified,
               ('"' || activity_profile_document_sha.sha1_digest || '"') AS etag
          FROM activity_profile_document 
          LEFT JOIN activity_profile_document_sha
            ON activity_profile_document.id = activity_profile_document_sha.doc_id
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
    """)
    suspend fun findETagAndLastModifiedByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    ): ETagAndLastModifiedAsStrings?

    @Query(
        """
        SELECT * 
          FROM activity_profile_document 
         WHERE id = :id
        """
    )
    suspend fun findById(id: String): XapiActivityProfileDocumentEntity?

    @Query(
        """
        SELECT profile_id 
          FROM activity_profile_document 
         WHERE activity_iri = :activityIri 
           AND (:since IS NULL OR last_modified > :since)
        """
    )
    suspend fun getProfileIds(activityIri: String, since: InstantAsTimestampString? = null): List<String>

    @Query(
        """
        DELETE FROM activity_profile_document
         WHERE activity_iri = :activityIri 
           AND profile_id = :profileId
        """
    )
    suspend fun deleteByActivityIriAndProfileId(
        activityIri: String,
        profileId: String,
    )
}