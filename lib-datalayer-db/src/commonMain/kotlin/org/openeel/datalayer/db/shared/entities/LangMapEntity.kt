package world.respect.datalayer.db.shared.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents entries in a LangMap structure as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#42-language-maps
 *
 * LangMaps are used on the RespectAppManifest, OPDS, and various Xapi entities.
 *
 * @property lmeTopParentType a unique table id (e.g. using TABLE_ID constants)
 * @property lmeTopParentUid1 the uid of the related entity (e.g. RespectAppManifest, OpdsPublication, XapiStatement, etc)
 * @property lmeTopParentUid2 second uid of the related entity, where applicable (e.g. Xapi Statements UUID)
 * @property lmePropType Entities have multiple properties that use a LangMap, this field can be used to differentiate
 * @property lmePropFk when related to an entity that is not directly the topParent e.g.
 *           ReadiumSubject name, then this is the foreign key for the entity, otherwise 0 (e.g.
 *           when the LangMap is directly related to the top parent).
 * @property lmeLang Language code (e.g. en)
 * @property lmeRegion Region code (e.g. US)
 * @property lmeValue the actual string value
 */
@Entity(
    indices = [Index(value = ["lmeTopParentType", "lmeTopParentUid1", "lmeTopParentUid2", "lmePropType"])]
)
data class LangMapEntity(
    @PrimaryKey(autoGenerate = true)
    val lmeId: Long = 0,
    val lmeTopParentType: TopParentType,
    val lmeTopParentUid1: Long,
    val lmeTopParentUid2: Long = 0,
    val lmePropType: PropType,
    val lmePropFk: Long,
    val lmeLang: String,
    val lmeRegion: String?,
    val lmeValue: String,
) {

    enum class TopParentType(val id: Int) {
        RESPECT_MANIFEST(RESPECT_MANIFEST_PARENT_ID),
        OPDS_FEED(OPDS_FEED_PARENT_ID),
        OPDS_PUBLICATION(ODPS_PUBLICATION_PARENT_ID),
        RESPECT_SCHOOL_DIRECTORY_ENTRY(RESPECT_SCHOOL_DIRECTORY_ENTRY_ID),
    }

    enum class PropType(val id: Int) {
        RESPECT_MANIFEST_NAME(1), RESPECT_MANIFEST_DESCRIPTION(2),

        READIUM_SUBJECT_NAME(3),

        OPDS_PUB_TITLE(4), OPDS_PUB_SORT_AS(5), OPDS_PUB_SUBTITLE(6),

        RESPECT_SCHOOL_DIRECTORY_ENTRY_NAME(7),
    }


    companion object {

        const val RESPECT_MANIFEST_PARENT_ID = 1

        const val OPDS_FEED_PARENT_ID = 2

        const val ODPS_PUBLICATION_PARENT_ID = 3

        const val RESPECT_SCHOOL_DIRECTORY_ENTRY_ID = 4

        const val LANG_NONE = ""

    }

}