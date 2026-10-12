package org.openeel.datalayer.school.ext

import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.PersonRoleEnum


val EnrollmentRoleEnum.relatedPersonRoleEnum: PersonRoleEnum
    get() = when(this) {
        EnrollmentRoleEnum.STUDENT, EnrollmentRoleEnum.PENDING_STUDENT -> PersonRoleEnum.STUDENT
        EnrollmentRoleEnum.TEACHER, EnrollmentRoleEnum.PENDING_TEACHER -> PersonRoleEnum.TEACHER
    }
