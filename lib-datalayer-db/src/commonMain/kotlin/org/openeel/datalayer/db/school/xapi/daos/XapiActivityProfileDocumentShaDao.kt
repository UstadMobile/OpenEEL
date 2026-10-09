package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentShaEntity

@Dao
interface XapiActivityProfileDocumentShaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiActivityProfileDocumentShaEntity)

    @Query(
        """
        SELECT * 
          FROM activity_profile_document_sha 
         WHERE doc_id = :docId
        """
    )
    suspend fun findByDocId(docId: String): XapiActivityProfileDocumentShaEntity?

    @Query(
        """
        DELETE FROM activity_profile_document_sha
         WHERE doc_id = :docId
        """
    )
    suspend fun deleteByDocId(docId: String)
}
