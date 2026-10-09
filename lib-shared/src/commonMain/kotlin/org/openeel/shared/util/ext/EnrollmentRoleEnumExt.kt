package org.openeel.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.pending_student
import org.openeel.shared.generated.resources.pending_teacher
import org.openeel.shared.generated.resources.student
import org.openeel.shared.generated.resources.teacher

val EnrollmentRoleEnum.label: StringResource
    get() = when(this) {
        EnrollmentRoleEnum.STUDENT -> Res.string.student
        EnrollmentRoleEnum.TEACHER -> Res.string.teacher
        EnrollmentRoleEnum.PENDING_STUDENT -> Res.string.pending_student
        EnrollmentRoleEnum.PENDING_TEACHER -> Res.string.pending_teacher
    }
