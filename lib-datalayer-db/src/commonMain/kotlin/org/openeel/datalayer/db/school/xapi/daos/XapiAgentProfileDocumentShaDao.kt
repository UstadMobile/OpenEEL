package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentShaEntity

@Dao
interface XapiAgentProfileDocumentShaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiAgentProfileDocumentShaEntity)

    @Query(
        """
        SELECT * 
          FROM agent_profile_document_sha 
         WHERE doc_id = :docId
        """
    )
    suspend fun findByDocId(docId: String): XapiAgentProfileDocumentShaEntity?

    @Query(
        """
        DELETE FROM agent_profile_document_sha
         WHERE doc_id = :docId
        """
    )
    suspend fun deleteByDocId(docId: String)
}
