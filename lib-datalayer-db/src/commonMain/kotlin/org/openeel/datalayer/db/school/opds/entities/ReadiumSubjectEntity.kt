package world.respect.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eygraber.uri.Uri
import world.respect.datalayer.db.school.opds.OpdsParentType

/**
 * @property rseStringValue a ReadiumSubject can be a simple string value type or an object type.
 *           If its a simple string value, then the stringValue is stored here. If its an object
 *           rseStringValue is null
 */
@Entity
data class ReadiumSubjectEntity(
    @PrimaryKey(autoGenerate = true)
    val rseUid: Long,
    val rseStringValue: String?,
    val rseTopParentType: OpdsParentType,
    val rseTopParentUid: Long,
    val rseSubjectSortAs: String?,
    val rseSubjectCode: String?,
    val rseSubjectScheme: Uri?,
    val rseIndex: Int,
) {

    companion object {

        const val TABLE_ID = 9

    }

}
