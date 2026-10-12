package org.openeel.datalayer.school.ext

import org.openeel.datalayer.school.model.Enrollment
import org.openeel.datalayer.school.model.EnrollmentRoleEnum

fun Enrollment.copyAsApproved(): Enrollment {
    val currentRole = role
    return copy(
        role = when (currentRole) {
            EnrollmentRoleEnum.PENDING_TEACHER -> EnrollmentRoleEnum.TEACHER
            EnrollmentRoleEnum.PENDING_STUDENT -> EnrollmentRoleEnum.STUDENT
            else -> currentRole
        }
    )
}