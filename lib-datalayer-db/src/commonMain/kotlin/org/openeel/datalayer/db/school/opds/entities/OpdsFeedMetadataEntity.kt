package world.respect.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.eygraber.uri.Uri
import kotlinx.datetime.LocalDateTime
import kotlin.time.Instant

/**
 * @property ofmeUid auto generated primary key
 * @property ofmeOfeUid the OpdsFeed uid (always set, even if this is for a facet or group)
 * @property ofmePropType the property type of this metadata
 * @property ofmePropFk the uid of the entity as per property type e.g. OpdsFeed uid, OpdsFacet uid,
 *           OpdsGroup uid. If propType is FEED_METADATA, then ofmePropFk = ofmeOfeUid
 */
@Entity
data class OpdsFeedMetadataEntity(
    @PrimaryKey(autoGenerate = true)
    val ofmeUid: Long = 0,
    val ofmeOfeUid: Long,
    val ofmePropType: PropType,
    val ofmePropFk: Long,
    val ofmeIdentifier: Uri?,
    val ofmeType: String?,
    val ofmeTitle: String,
    val ofmeSubtitle: String?,
    val ofmeModified: Instant?,
    val ofmeDescription: String?,
    val ofmeItemsPerPage: Int?,
    val ofmeCurrentPage: Int?,
    val ofmeNumberOfItems: Int?
) {

    enum class PropType(val id: Int) {
        FEED_METADATA(1), FACET_METADATA(2), GROUP_METADATA(3)
    }

}