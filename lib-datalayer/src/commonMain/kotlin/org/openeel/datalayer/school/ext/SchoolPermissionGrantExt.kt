package world.respect.datalayer.school.ext

import world.respect.datalayer.school.model.SchoolPermissionGrant

/**
 * Given a list of SchoolPermissionGrants and a list of permission flags, filter the list of
 * permission flags to those that are included in the list of SchoolPermissionGrants
 */
fun List<SchoolPermissionGrant>.filterPermissions(
    permissionFlags: List<Long>
): List<Long> {
    return permissionFlags.filter { flag ->
        this.any { it.permissions.and(flag) == flag  }
    }
}

