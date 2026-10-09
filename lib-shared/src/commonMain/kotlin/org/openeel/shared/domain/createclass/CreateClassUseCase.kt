package world.respect.shared.domain.createclass

import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.model.ClassInvite
import world.respect.datalayer.school.model.ClassInviteModeEnum
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.Invite2

/**
 * Use case to contain logic for creating a new class.
 *
 * Currently:
 *
 * 1) Stores the class itself
 * 2) Stores invites for the class for each role
 */
class CreateClassUseCase(
    private val dataSource: SchoolDataSource
) {

    suspend operator fun invoke(
        clazz: Clazz
    ) {
        dataSource.classDataSource.store(listOf(clazz))

        dataSource.inviteDataSource.store(
            listOf(
                Pair(EnrollmentRoleEnum.TEACHER, ClassInviteModeEnum.DIRECT),
                Pair(EnrollmentRoleEnum.STUDENT, ClassInviteModeEnum.DIRECT),
                Pair(EnrollmentRoleEnum.STUDENT, ClassInviteModeEnum.VIA_PARENT),
            ).map { (role, inviteMode) ->
                ClassInvite(
                    uid = ClassInvite.uidFor(
                        clazz.guid, role, inviteMode = inviteMode
                    ),
                    code = Invite2.newRandomCode(),
                    classUid = clazz.guid,
                    role = role,
                    inviteMode = inviteMode,
                )
            }
        )
    }
}