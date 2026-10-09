package world.respect.datalayer.school.ext

import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.FamilyMemberInvite
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.model.NewUserInvite
import world.respect.datalayer.school.model.PersonRoleEnum
import kotlin.time.Clock
import kotlin.time.Instant

/**
 * Make a copy of the given invite with an updated approval required after time.
 */
fun Invite2.copyInvite(
    approvalRequiredAfter: Instant = this.approvalRequiredAfter,
    code: String = this.code,
    lastModified: Instant = this.lastModified,
): Invite2 {
    return when(this) {
        is NewUserInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            code = code,
            lastModified = lastModified
        )
        is FamilyMemberInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            code = code,
            lastModified = lastModified
        )
        is ClassInvite -> copy(
            approvalRequiredAfter = approvalRequiredAfter,
            code = code,
            lastModified = lastModified
        )
    }
}

fun Invite2.isApprovalRequiredNow(): Boolean = approvalRequiredAfter < Clock.System.now()

fun Invite2.isChildUser(): Boolean {
    return this.accepterPersonRole == PersonRoleEnum.STUDENT
}

/**
 * The PersonRoleEnum that the accepter of this invite is expected to have.
 */
val Invite2.accepterPersonRole: PersonRoleEnum
    get() =when(this) {
        is NewUserInvite -> this.role
        is ClassInvite -> if(inviteMode == ClassInviteModeEnum.VIA_PARENT) {
            PersonRoleEnum.PARENT
        }else {
            this.role.relatedPersonRoleEnum
        }
        is FamilyMemberInvite -> PersonRoleEnum.PARENT
    }


fun Invite2.accepterEnrollmentRole(
    approvalRequired: Boolean = isApprovalRequiredNow()
) : EnrollmentRoleEnum? {
    if(this !is ClassInvite)
        return null

    return when {
        approvalRequired && this.role == EnrollmentRoleEnum.STUDENT -> EnrollmentRoleEnum.PENDING_STUDENT
        approvalRequired && this.role == EnrollmentRoleEnum.TEACHER -> EnrollmentRoleEnum.PENDING_TEACHER
        else -> this.role
    }
}
