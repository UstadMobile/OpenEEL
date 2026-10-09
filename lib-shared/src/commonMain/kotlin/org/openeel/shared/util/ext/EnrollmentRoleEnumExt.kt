package world.respect.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.pending_student
import world.respect.shared.generated.resources.pending_teacher
import world.respect.shared.generated.resources.student
import world.respect.shared.generated.resources.teacher

val EnrollmentRoleEnum.label: StringResource
    get() = when(this) {
        EnrollmentRoleEnum.STUDENT -> Res.string.student
        EnrollmentRoleEnum.TEACHER -> Res.string.teacher
        EnrollmentRoleEnum.PENDING_STUDENT -> Res.string.pending_student
        EnrollmentRoleEnum.PENDING_TEACHER -> Res.string.pending_teacher
    }
