package world.respect.datalayer.school.ext

import world.respect.datalayer.school.model.Invite2.Companion.TYPE_NEW_USER
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.school.model.PersonRoleEnum

/**
 * The permission flag required to change a person with this role
 */
val PersonRoleEnum.writePermissionFlag: Long
    get() = when(this) {
        PersonRoleEnum.STUDENT -> PermissionFlags.PERSON_STUDENT_WRITE
        PersonRoleEnum.TEACHER -> PermissionFlags.PERSON_TEACHER_WRITE
        PersonRoleEnum.PARENT -> PermissionFlags.PERSON_PARENT_WRITE
        else -> PermissionFlags.PERSON_ADMIN_WRITE
    }

/**
 * The uid to use for a new user invite.
 */
val PersonRoleEnum.newUserInviteUid: String
    get() = "$TYPE_NEW_USER:${this.value}"


/**
 * Fold the list of PersonRoleEnum into a single flag
 */
fun List<PersonRoleEnum?>.foldToFlag(): Int {
    return this.fold(0) { acc, enum ->
        if(enum != null) {
            acc.or(enum.flag)
        }else {
            acc
        }
    }
}
