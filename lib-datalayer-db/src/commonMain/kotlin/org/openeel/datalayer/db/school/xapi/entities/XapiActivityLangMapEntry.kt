package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable
import world.respect.datalayer.UidNumberMapper
import kotlin.time.Clock
import kotlin.time.Instant

@Serializable
@Entity(
    primaryKeys = ["almeActivityUid", "almeKeyHash"],
    indices = [
        Index(
            value = ["almeActivityUid", "almeProperty", "almeInteractionId", "almeLangCode"],
            unique = true
        )
    ]
)
/**
 * As per Activity Definition:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#activity-definition
 *
 * An Activity has a Name and Description property that are LangMaps. The Activity can also have
 * interaction properties (e.g. choices), each of which can have a name.
 *
 * ActivityLangMapEntry is used to store all of these.
 *
 * The primary keys are the activity uid and the key hash: this makes it possible to update
 * definitions as/when they are seen, including when this happens to different languages and
 * properties in different requests.
 *
 * @param almeActivityUid the activity uid that this lang map entry is related to
 * @param almeKeyHash a hash of the property, interaction id, and lang code.
 * @param almeLangCode the lang code as per the xAPI language map eg en-US
 * @param almeProperty the property that this LangMapEntry is for : see ActivityLangMapEntryPropEnum
 * @param almeInteractionId if this is an entry for an interaction, then the id of the interaction
 * @param almeValue the string value for the given language
 * @property almeLastModified the time when this entry was last modified: used for conflict resolution
 */
data class XapiActivityLangMapEntry(
    val almeActivityUid: Long = 0,

    val almeKeyHash: Long = 0,

    val almeProperty: XapiActivityLangMapEntryPropEnum,

    val almeInteractionId: String?,

    val almeLangCode: String,

    val almeValue: String,

    val almeLastModified: Instant = Clock.System.now(),
) {
    companion object {
        const val TABLE_ID = 6442

        fun keyHashFor(
            uidNumberMapper: UidNumberMapper,
            property: XapiActivityLangMapEntryPropEnum,
            almeInteractionId: String?,
            almeLangCode: String,
        ) = uidNumberMapper(
            "${property.flag}${almeInteractionId?:""}$almeLangCode"
        )

    }
}

