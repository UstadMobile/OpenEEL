package world.respect.datalayer.db.school.xapi.daos

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import world.respect.datalayer.db.school.xapi.entities.XapiStateDocumentShaEntity

@Dao
interface XapiStateDocumentShaDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: XapiStateDocumentShaEntity)

    @Query(
        """
        SELECT * 
          FROM state_document_sha 
         WHERE doc_id = :docId
        """
    )
    suspend fun findByDocId(docId: String): XapiStateDocumentShaEntity?

    @Query(
        """
        DELETE FROM state_document_sha
         WHERE doc_id = :docId
        """
    )
    suspend fun deleteByDocId(docId: String)
}
