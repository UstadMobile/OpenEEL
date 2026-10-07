package org.openeel.datalayer.db.school.domain

import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase.PermissionsRequiredByRole
import org.openeel.datalayer.school.model.PersonRoleEnum

class CheckPersonPermissionUseCaseDbImpl(
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolDb: SchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
): CheckPersonPermissionUseCase {

    override suspend fun invoke(
        otherPersonUid: String,
        otherPersonKnownRole: PersonRoleEnum?,
        permissionsRequiredByRole: PermissionsRequiredByRole,
    ): Boolean {
        return schoolDb.getPersonEntityDao().getLastModifiedAndHasPermission(
            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
            personUidNum = uidNumberMapper(otherPersonUid),
            knownRoleFlag = otherPersonKnownRole?.flag ?: 0,
            roleAdminPermissionRequired = permissionsRequiredByRole.roleAdminPermissionRequired,
            roleTeacherPermissionRequired = permissionsRequiredByRole.roleTeacherPermissionRequired,
            roleParentPermissionRequired = permissionsRequiredByRole.roleParentPermissionRequired,
            roleStudentPermissionRequired = permissionsRequiredByRole.roleStudentPermissionRequired
        ).hasPermission
    }
}