package world.respect.datalayer.school.ext

import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PersonRoleEnum


val EnrollmentRoleEnum.relatedPersonRoleEnum: PersonRoleEnum
    get() = when(this) {
        EnrollmentRoleEnum.STUDENT, EnrollmentRoleEnum.PENDING_STUDENT -> PersonRoleEnum.STUDENT
        EnrollmentRoleEnum.TEACHER, EnrollmentRoleEnum.PENDING_TEACHER -> PersonRoleEnum.TEACHER
    }
