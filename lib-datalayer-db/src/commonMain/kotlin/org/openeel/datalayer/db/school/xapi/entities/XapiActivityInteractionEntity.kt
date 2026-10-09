package world.respect.datalayer.db.school.xapi.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

/**
 * Represents an interaction component as per
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Data.md#interaction-activities
 *
 * Used as a 1:many join with ActivityEntity (aieActivityUid is the foreign key)
 *
 * @param aieActivityUid activity uid (foreign key)
 * @param aieProp Enum to identify which property this interaction entity is associated with -
 *        choices, scale, source, target, or steps.
 * @param aieId the id of this choice as per the spec
 */
@Entity
@Serializable
data class XapiActivityInteractionEntity(
    @PrimaryKey(autoGenerate = true)
    val aieUid: Long = 0,
    @ColumnInfo(index = true)
    val aieActivityUid: Long = 0,
    val aieProp: XapiActivityInteractionEntityPropEnum,
    val aieId: String,
)
