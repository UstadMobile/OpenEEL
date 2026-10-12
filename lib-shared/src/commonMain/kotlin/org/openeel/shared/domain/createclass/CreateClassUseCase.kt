package org.openeel.shared.domain.createclass

import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.ClassInviteModeEnum
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.Invite2

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