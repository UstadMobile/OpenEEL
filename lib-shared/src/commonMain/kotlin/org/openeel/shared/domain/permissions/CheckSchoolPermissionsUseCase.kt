package org.openeel.shared.domain.permissions

import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.SchoolPermissionGrantDataSource
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase.PermissionsRequiredByRole
import org.openeel.datalayer.school.ext.filterPermissions

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