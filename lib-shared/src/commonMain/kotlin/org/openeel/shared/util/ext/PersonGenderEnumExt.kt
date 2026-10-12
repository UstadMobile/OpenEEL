package org.openeel.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.female
import org.openeel.shared.generated.resources.male
import org.openeel.shared.generated.resources.other
import org.openeel.shared.generated.resources.unspecified

val PersonGenderEnum.label: StringResource
    get() = when(this) {
        PersonGenderEnum.MALE -> Res.string.male
        PersonGenderEnum.FEMALE -> Res.string.female
        PersonGenderEnum.OTHER -> Res.string.other
        PersonGenderEnum.UNSPECIFIED -> Res.string.unspecified
    }
