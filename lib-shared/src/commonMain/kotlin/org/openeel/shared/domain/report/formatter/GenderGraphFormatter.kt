package world.respect.shared.domain.report.formatter

import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.female
import world.respect.shared.generated.resources.male
import world.respect.shared.generated.resources.other
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.resources.UiText

const val GENDER_FEMALE = 1
const val GENDER_MALE = 2

class GenderGraphFormatter : GraphFormatter<String> {

    override fun adjust(value: String): String {
        return value
    }

    override fun format(value: String): UiText {
        return when (value) {
            GENDER_FEMALE.toString() -> StringResourceUiText(Res.string.female)
            GENDER_MALE.toString() -> StringResourceUiText(Res.string.male)
            else -> StringResourceUiText(Res.string.other)
        }
    }
}

