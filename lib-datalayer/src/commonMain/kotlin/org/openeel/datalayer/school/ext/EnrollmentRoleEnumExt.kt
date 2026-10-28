package org.openeel.datalayer.school.ext

import org.openeel.lib.directory.model.EnrollmentRoleEnum
import org.openeel.lib.directory.model.PersonRoleEnum


val EnrollmentRoleEnum.relatedPersonRoleEnum: PersonRoleEnum
    get() = when(this) {
        EnrollmentRoleEnum.STUDENT, EnrollmentRoleEnum.PENDING_STUDENT -> PersonRoleEnum.STUDENT
        EnrollmentRoleEnum.TEACHER, EnrollmentRoleEnum.PENDING_TEACHER -> PersonRoleEnum.TEACHER
    }
