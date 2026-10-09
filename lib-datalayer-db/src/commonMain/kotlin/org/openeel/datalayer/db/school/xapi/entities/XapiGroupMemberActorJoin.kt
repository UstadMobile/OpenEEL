package world.respect.datalayer.db.school.xapi.entities

import androidx.room.Entity
import androidx.room.Index
import kotlinx.serialization.Serializable

/**
 * See XapiGroup.toGroupEntities for further details on the mapping between the Xapi Spec and the
 * database entities.
 */
@Entity(
    primaryKeys = ["gmajGroupActorUid", "gmajMemberActorUid"],
    indices = [
        Index("gmajGroupActorUid", name = "idx_groupmemberactorjoin_gmajgroupactoruid"),
        Index("gmajMemberActorUid", name = "idx_groupmemberactorjoin_gmajmemberactoruid")
    ]
)
@Serializable
data class XapiGroupMemberActorJoin(
    val gmajGroupActorUid: Long,
    val gmajMemberActorUid: Long,
) {
    companion object {
        const val TABLE_ID = 4232
    }
}
