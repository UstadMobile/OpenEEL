package world.respect.datalayer.respect.model.invite

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Person


/**
 * @property guid GUID for the invite redemption request itself
 * @property userPerson Person representing the person who redeemed the invite
 * @property childPerson if the invite was redeemed as a parent, then the child person is included,
 *           otherwise, null.
 */
@Serializable
class RespectPendingInviteState(
    val guid: String,
    val userPerson: Person,
    val childPerson: Person?,
    val status: Status,
    val lastModified: Long,
) {

    enum class Status {

        PENDING, APPROVED, DECLINED

    }

}

