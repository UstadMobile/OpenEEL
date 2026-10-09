package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import kotlinx.serialization.Serializable

@Entity(
    primaryKeys = ["vlmeVerbUid", "vlmeLangCode"]
)
@Serializable
/**
 * Use in a one to many join with VerbEntity. Verb display can be updated. When statements are
 * queried using canonical mode, we need to be able to return the the latest display langmap.
 *
 * @param vlmeVerbUid the foreign key e.g. VerbEntity.verbUid (xxhash of the Verb's id url)
 * @param vlmeEntryString the actual string e.g. as will be displayed to the user e.g. 'Completed'
 * @param vlmeLangCode the lang code as per the Language Map
 */
data class XapiVerbLangMapEntry(
    val vlmeVerbUid: Long = 0L,
    val vlmeLangCode: String,
    val vlmeEntryString: String,
) {
    companion object {
    }
}
