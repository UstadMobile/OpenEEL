package org.openeel.shared.domain.report.formatter

import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.female
import org.openeel.shared.generated.resources.male
import org.openeel.shared.generated.resources.other
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.resources.UiText

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

