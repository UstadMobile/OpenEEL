package world.respect.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.parent
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.generated.resources.site_administrator
import world.respect.shared.generated.resources.student
import world.respect.shared.generated.resources.system_administrator
import world.respect.shared.generated.resources.teacher

val PersonRoleEnum.label: StringResource
    get() = when(this) {
        PersonRoleEnum.PARENT -> Res.string.parent
        PersonRoleEnum.STUDENT -> Res.string.student
        PersonRoleEnum.TEACHER -> Res.string.teacher
        PersonRoleEnum.SYSTEM_ADMINISTRATOR -> Res.string.system_administrator
        PersonRoleEnum.SITE_ADMINISTRATOR -> Res.string.site_administrator
        PersonRoleEnum.SHARED_SCHOOL_DEVICE -> Res.string.shared_school_devices
    }