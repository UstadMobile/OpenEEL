package world.respect.datalayer.db.school.domain

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase.PermissionsRequiredByRole
import world.respect.datalayer.school.model.PersonRoleEnum

class CheckPersonPermissionUseCaseDbImpl(
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val schoolDb: RespectSchoolDatabase,
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
            roleStudentPermissionRequired = permissionsRequiredByRole.roleStudentPermissionRequired,
            roleSharedDevicePermissionRequired =  permissionsRequiredByRole.roleSharedDevicePermissionRequired
        ).hasPermission
    }
}