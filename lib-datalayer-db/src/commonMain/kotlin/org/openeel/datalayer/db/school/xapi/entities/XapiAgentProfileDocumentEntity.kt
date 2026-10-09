package world.respect.datalayer.db.school.xapi.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.ktor.util.date.GMTDate
import world.respect.datalayer.db.shared.InstantAsTimestampString
import world.respect.lib.dataloadstate.datetime.toGMTDate
import world.respect.lib.xapi.model.XapiDocument
import kotlin.uuid.Uuid

/**
 * The id field is a random UUID : same behavior as SQL LRS.
 */
@Entity(
    tableName = "agent_profile_document"
)
data class XapiAgentProfileDocumentEntity(
    @PrimaryKey
    val id: String = Uuid.random().toString(),

    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "agent_ifi")
    val agentIfi: String,

    @ColumnInfo(name = "content_type")
    val contentType: String,

    val contents: ByteArray,

    @ColumnInfo(name = "content_length")
    val contentLength: Int = contents.size,

    @ColumnInfo(name = "last_modified")
    val lastModified: InstantAsTimestampString,
): XapiDocument {

    override val type: String
        get() = contentType

    override val updated: GMTDate by lazy {
        lastModified.instant.toGMTDate()
    }

    override suspend fun contentsAsByteArray(): ByteArray = contents

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as XapiAgentProfileDocumentEntity

        if (contentLength != other.contentLength) return false
        if (id != other.id) return false
        if (profileId != other.profileId) return false
        if (agentIfi != other.agentIfi) return false
        if (contentType != other.contentType) return false
        if (!contents.contentEquals(other.contents)) return false
        if (lastModified != other.lastModified) return false
        if (updated != other.updated) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contentLength
        result = 31 * result + id.hashCode()
        result = 31 * result + profileId.hashCode()
        result = 31 * result + agentIfi.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + contents.contentHashCode()
        result = 31 * result + lastModified.hashCode()
        result = 31 * result + updated.hashCode()
        return result
    }

}