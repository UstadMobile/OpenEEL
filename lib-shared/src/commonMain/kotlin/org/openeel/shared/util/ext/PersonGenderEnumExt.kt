package world.respect.shared.util.ext

import org.jetbrains.compose.resources.StringResource
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.female
import world.respect.shared.generated.resources.male
import world.respect.shared.generated.resources.other
import world.respect.shared.generated.resources.unspecified

val PersonGenderEnum.label: StringResource
    get() = when(this) {
        PersonGenderEnum.MALE -> Res.string.male
        PersonGenderEnum.FEMALE -> Res.string.female
        PersonGenderEnum.OTHER -> Res.string.other
        PersonGenderEnum.UNSPECIFIED -> Res.string.unspecified
    }
