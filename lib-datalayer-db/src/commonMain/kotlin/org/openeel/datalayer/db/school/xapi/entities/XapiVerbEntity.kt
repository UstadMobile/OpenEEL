package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.StatusEnum

@Serializable
@Entity
/**
 * Verb as per the xAPI spec. Verb only has two properties ( id and display ) as per the spec:
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#243-verb
 *
 * Joins with VerbXLangMapEntry to handle the display langmap.
 *
 * No VerbEntity will be saved if the display property is defined as per allDefinedVerbs.
 *
 * @param verbUid The XXHash64 of verbUrlId
 */
data class XapiVerbEntity(
    @PrimaryKey
    val verbUid: Long,
    val verbUrlId: String,
    val verbStatus: StatusEnum = StatusEnum.ACTIVE,
    val verbLct: Long = 0,
) {
    companion object {
        const val TABLE_ID = 62

    }
}
