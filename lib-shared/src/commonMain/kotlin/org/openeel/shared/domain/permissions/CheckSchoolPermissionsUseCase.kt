package world.respect.shared.domain.permissions

import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.school.SchoolPermissionGrantDataSource
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase.PermissionsRequiredByRole
import world.respect.datalayer.school.ext.filterPermissions

/**
 * Use case to check what school wide permissions are available to the authenticated user. This is
 * scoped in dependency injection to the account level so it returns values for the active account.
 */
class CheckSchoolPermissionsUseCase(
    private val schoolDataSource: SchoolDataSource
) {

    /**
     * @param permissionFlags a list of permission flags to check to see if the authenticated user
     *        has at a school-wide level.
     * @return a list of the permission flags available to the authenticated user.
     */
    suspend operator fun invoke(
        permissionFlags: List<Long>
    ): List<Long> {
        return schoolDataSource.schoolPermissionGrantDataSource.list(
            loadParams = DataLoadParams(onlyIfCached = true),
            params = SchoolPermissionGrantDataSource.GetListParams()
        ).dataOrNull()?.filterPermissions(
            permissionFlags = PermissionsRequiredByRole.WRITE_PERMISSIONS.flagList
        ) ?: emptyList()
    }

}