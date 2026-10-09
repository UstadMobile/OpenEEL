package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.composites.XapiStateDocumentAndShaEntities
import world.respect.datalayer.db.school.xapi.entities.XapiStateDocumentEntity
import world.respect.datalayer.db.shared.ETagAndLastModifiedAsStrings
import world.respect.datalayer.db.shared.InstantAsTimestampString
import kotlin.uuid.Uuid

@Dao
interface XapiStateDocumentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiStateDocumentEntity)

    @Query(
        """
        SELECT state_document.*, 
               state_document_sha.sha1_digest AS sha1
          FROM state_document 
               JOIN state_document_sha
                    ON state_document_sha.doc_id = state_document.id
         WHERE activity_iri = :activityIri 
           AND agent_ifi = :agentIfi
           AND ((:registration IS NULL AND registration IS NULL) OR registration = :registration)
           AND state_id = :stateId
        """
    )
    suspend fun findByActivityIriAndAgentIfiAndRegistrationAndStateId(
        activityIri: String,
        agentIfi: String,
        registration: Uuid?,
        stateId: String,
    ): XapiStateDocumentAndShaEntities?

    @Query("""
        SELECT state_document.last_modified AS lastModified,
               ('"' || state_document_sha.sha1_digest || '"') AS etag
          FROM state_document 
          LEFT JOIN state_document_sha
            ON state_document.id = state_document_sha.doc_id
         WHERE activity_iri = :activityIri 
           AND agent_ifi = :agentIfi
           AND ((:registration IS NULL AND registration IS NULL) OR registration = :registration)
           AND state_id = :stateId
    """)
    suspend fun findETagAndLastModifiedByActivityIriAndAgentIfiAndRegistrationAndStateId(
        activityIri: String,
        agentIfi: String,
        registration: Uuid?,
        stateId: String,
    ): ETagAndLastModifiedAsStrings?

    @Query(
        """
        SELECT * 
          FROM state_document 
         WHERE id = :id
        """
    )
    suspend fun findById(id: String): XapiStateDocumentEntity?

    @Query(
        """
        SELECT state_id 
          FROM state_document 
         WHERE activity_iri = :activityIri 
           AND agent_ifi = :agentIfi
           AND ((:registration IS NULL AND registration IS NULL) OR registration = :registration)
           AND (:since IS NULL OR last_modified > :since)
        """
    )
    suspend fun getStateIds(
        activityIri: String,
        agentIfi: String,
        registration: Uuid?,
        since: InstantAsTimestampString? = null,
    ): List<String>

    @Query(
        """
        DELETE FROM state_document
         WHERE activity_iri = :activityIri 
           AND agent_ifi = :agentIfi
           AND ((:registration IS NULL AND registration IS NULL) OR registration = :registration)
           AND state_id = :stateId
        """
    )
    suspend fun deleteByActivityIriAndAgentIfiAndRegistrationAndStateId(
        activityIri: String,
        agentIfi: String,
        registration: Uuid?,
        stateId: String,
    )
}
