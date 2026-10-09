package org.openeel.datalayer.db.school.domain

import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.school.model.PermissionFlags
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.school.model.SchoolPermissionGrant

/**
 * Add Default School Permission Grants for each person role.
 *
 * This is only used on the server side. It may be used before any user is
 * created.
 */
class AddDefaultSchoolPermissionGrantsUseCase(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
) {

    private fun PersonRoleEnum.newInitialGrant(
        permissions: Long
    ) = SchoolPermissionGrant(
        uid = this.flag.toString(),
        toRole = this,
        permissions = permissions,
    )

    suspend operator fun invoke() {
        schoolDb.getSchoolPermissionGrantDao().upsert(
            listOf(
                PersonRoleEnum.SYSTEM_ADMINISTRATOR.newInitialGrant(
                    PermissionFlags.SYSADMIN_DEFAULT_SCHOOL_PERMISSIONS
                ),
                PersonRoleEnum.TEACHER.newInitialGrant(
                    PermissionFlags.TEACHER_DEFAULT_SCHOOL_PERMISSIONS
                ),
                PersonRoleEnum.STUDENT.newInitialGrant(
                    PermissionFlags.STUDENT_DEFAULT_SCHOOL_PERMISSIONS
                ),
                PersonRoleEnum.PARENT.newInitialGrant(
                    PermissionFlags.PARENT_DEFAULT_SCHOOL_PERMISSIONS
                ),
            ).map {
                it.toEntity(uidNumberMapper)
            }
        )
    }
}