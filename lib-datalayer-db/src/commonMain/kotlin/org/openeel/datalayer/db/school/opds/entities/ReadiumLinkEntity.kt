package world.respect.datalayer.db.school.opds.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import world.respect.datalayer.db.school.opds.OpdsParentType

/**
 * Database Entity that represents a ReadiumLink. ReadiumLinks are always associated with an
 * OpdsFeed or OpdsPublication (handled by the rleTableId and rleEntityId fields).
 *
 * A ReadiumLink can join to other ReadiumLinks e.g. via children, alternate, or subcollection (
 * which is a recursive structure as per the ReadliumLink JSON spec). This is handled by the
 * rleJoinToLinkId and rleJoinToLinkType fields.
 *
 * @property rleOpdsParentType the top parent type (opds feed or publication)
 * @property rleOpdsParentUid entityId for the entity this is part of OpdsPublication or OpdsFeed
 * @property rlePropType The property type where this link has been used (can be properties of
 *           various OPDS related types including properties of ReadiumLink itself)
 * @property rlePropFk The foreign key for the entity of the type specified in rlePropType; where
 *           ReadiumLink is joined to itself this would be ReadiumLinkEntity.rleId . If this
 *           ReadiumLink is used by a direct attribute of OpdsFeed or OpdsPublication then
 *           rlePropFk will equal rleOpdsParentUid
 *
 */
@Entity
class ReadiumLinkEntity(
    @PrimaryKey(autoGenerate = true)
    val rleId: Long,

    val rleOpdsParentType: OpdsParentType,

    val rleOpdsParentUid: Long,

    val rlePropType: PropertyType,

    val rlePropFk: Long,

    val rleIndex: Int,

    val rleHref: String,

    val rleRel: List<String>?,

    val rleType: String? = null,

    val rleTitle: String? = null,

    val rleTemplated: Boolean? = null,

    val rleProperties: String? = null,

    val rleHeight: Int? = null,

    val rleWidth: Int? = null,

    val rleSize: Int? = null,

    val rleBitrate: Double? = null,

    val rleDuration: Double? = null,

    val rleLanguage: List<String>? = null,
) {

    /**
     * ReadiumLink can be contained directly by an OpdsFeed or OpdsPublication, or by subelements
     * thereof. The ReadiumLinkEntity.rleTableId and ReadiumLinkEntity.rleEntityUid fields will
     * ALWAYS connect to the OpdsFeed or OpdsPublication (such that they can be queried) accordingly.
     */
    @Suppress("unused")
    enum class PropertyType {
        LINK_ALTERNATE, LINK_CHILDREN, LINK_SUB_COLLECTION,

        OPDS_PUB_LINKS, OPDS_PUB_IMAGES, OPDS_PUB_READING_ORDER, OPDS_PUB_RESOURCES, OPDS_PUB_TOC,
        OPDS_PUB_SUBJECT_LINKS,

        OPDS_FACET_LINKS, OPDS_GROUP_LINKS, OPDS_GROUP_NAVIGATION,

        OPDS_FEED_LINKS, OPDS_FEED_NAVIGATION,

        READIUM_CONTRIBUTOR,
        READIUM_SUBJECT_LINKS
    }

    companion object {

        const val TABLE_ID = 2

    }

}