package world.respect.datalayer.respect.model.invite

import kotlinx.serialization.Serializable
import world.respect.datalayer.school.model.Invite2

/**
 * @property invite: The invite itself
 * @property classGuid The guid of the class to which the user is being invited
 * @property className The name of the class to which the user is being invited
 * @property userInviteType type of invite as per the enum
 */
@Serializable
class RespectInviteInfo(
    val classGuid: String?=null,
    val className: String?=null,
    val userInviteType: UserInviteType?=null,
    val invite: Invite2? = null
) {

    @Suppress("unused")
    enum class UserInviteType {
        TEACHER, STUDENT_OR_PARENT
    }
}