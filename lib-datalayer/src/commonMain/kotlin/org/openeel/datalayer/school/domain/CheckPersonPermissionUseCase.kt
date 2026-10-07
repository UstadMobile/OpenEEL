package org.openeel.datalayer.school.domain

import org.openeel.datalayer.school.model.PermissionFlags
import org.openeel.datalayer.school.model.PersonRoleEnum

/**
 * The CheckPersonUseCase is bound (scoped) to a specified authenticated person (the same as the
 * SchoolDataSource itself).
 */
interface CheckPersonPermissionUseCase {

    data class PermissionsRequiredByRole(
        val roleAdminPermissionRequired: Long = PermissionFlags.PERSON_ADMIN_WRITE,
        val roleTeacherPermissionRequired: Long = PermissionFlags.PERSON_TEACHER_WRITE,
        val roleStudentPermissionRequired: Long = PermissionFlags.PERSON_STUDENT_WRITE,
        val roleParentPermissionRequired: Long = PermissionFlags.PERSON_PARENT_WRITE,
    ) {

        val flagList: List<Long>
            get() = listOf(roleAdminPermissionRequired,
                roleTeacherPermissionRequired,
                roleStudentPermissionRequired,
                roleParentPermissionRequired
            )

        companion object {

            val WRITE_PERMISSIONS = PermissionsRequiredByRole(
                roleAdminPermissionRequired = PermissionFlags.PERSON_ADMIN_WRITE,
                roleTeacherPermissionRequired = PermissionFlags.PERSON_TEACHER_WRITE,
                roleStudentPermissionRequired = PermissionFlags.PERSON_STUDENT_WRITE,
                roleParentPermissionRequired =  PermissionFlags.PERSON_PARENT_WRITE,
            )

        }
    }

    /**
     * Check to see if the authenticated user has a permission on another person
     *
     */
    suspend operator fun invoke(
        otherPersonUid: String,
        otherPersonKnownRole: PersonRoleEnum?,
        permissionsRequiredByRole: PermissionsRequiredByRole,
    ): Boolean

}