package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.composites.XapiAgentProfileDocumentAndShaEntities
import world.respect.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentEntity
import world.respect.datalayer.db.shared.ETagAndLastModifiedAsStrings
import world.respect.datalayer.db.shared.InstantAsTimestampString

@Dao
interface XapiAgentProfileDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiAgentProfileDocumentEntity)

    @Query(
        """
        SELECT agent_profile_document.*, 
               agent_profile_document_sha.sha1_digest AS sha1
          FROM agent_profile_document 
               JOIN agent_profile_document_sha
                    ON agent_profile_document_sha.doc_id = agent_profile_document.id
         WHERE agent_ifi = :agentIfi 
           AND profile_id = :profileId
        """
    )
    suspend fun findByAgentIfiAndProfileId(
        agentIfi: String,
        profileId: String,
    ): XapiAgentProfileDocumentAndShaEntities?

    @Query("""
        SELECT agent_profile_document.last_modified AS lastModified,
               ('"' || agent_profile_document_sha.sha1_digest || '"') AS etag
          FROM agent_profile_document 
          LEFT JOIN agent_profile_document_sha
            ON agent_profile_document.id = agent_profile_document_sha.doc_id
         WHERE agent_ifi = :agentIfi 
           AND profile_id = :profileId
    """)
    suspend fun findETagAndLastModifiedByAgentIfiAndProfileId(
        agentIfi: String,
        profileId: String,
    ): ETagAndLastModifiedAsStrings?

    @Query(
        """
        SELECT * 
          FROM agent_profile_document 
         WHERE id = :id
        """
    )
    suspend fun findById(id: String): XapiAgentProfileDocumentEntity?

    @Query(
        """
        SELECT profile_id 
          FROM agent_profile_document 
         WHERE agent_ifi = :agentIfi 
           AND (:since IS NULL OR last_modified > :since)
        """
    )
    suspend fun getProfileIds(agentIfi: String, since: InstantAsTimestampString? = null): List<String>

    @Query(
        """
        DELETE FROM agent_profile_document
         WHERE agent_ifi = :agentIfi 
           AND profile_id = :profileId
        """
    )
    suspend fun deleteByAgentIfiAndProfileId(
        agentIfi: String,
        profileId: String,
    )
}
