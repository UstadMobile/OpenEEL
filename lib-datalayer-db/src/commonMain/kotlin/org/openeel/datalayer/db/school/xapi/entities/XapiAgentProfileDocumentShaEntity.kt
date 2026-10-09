package world.respect.datalayer.db.school.xapi.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "agent_profile_document_sha"
)
/**
 * Table used to cache the SHA-1 digest of XAPI agent profile documents. This is kept as a separate
 * table because it is not included in agent_profile_document table as per the SQL LRS schema.
 *
 * @property docId the ID of the document as per [XapiActivityProfileDocumentEntity.id]
 * @property sha1digest the SHA-1 Digest of the contents of the document as a hexadecimal string
 */
data class XapiAgentProfileDocumentShaEntity(
    @PrimaryKey
    @ColumnInfo(name = "doc_id")
    val docId: String,

    @ColumnInfo(name = "sha1_digest")
    val sha1digest: String,
)

