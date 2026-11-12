package org.openeel.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.parent
import org.openeel.shared.generated.resources.shared_school_devices
import org.openeel.shared.generated.resources.site_administrator
import org.openeel.shared.generated.resources.student
import org.openeel.shared.generated.resources.system_administrator
import org.openeel.shared.generated.resources.teacher

val PersonRoleEnum.label: StringResource
    get() = when(this) {
        PersonRoleEnum.PARENT -> Res.string.parent
        PersonRoleEnum.STUDENT -> Res.string.student
        PersonRoleEnum.TEACHER -> Res.string.teacher
        PersonRoleEnum.SYSTEM_ADMINISTRATOR -> Res.string.system_administrator
        PersonRoleEnum.SITE_ADMINISTRATOR -> Res.string.site_administrator
        PersonRoleEnum.SHARED_SCHOOL_DEVICE -> Res.string.shared_school_devices
    }