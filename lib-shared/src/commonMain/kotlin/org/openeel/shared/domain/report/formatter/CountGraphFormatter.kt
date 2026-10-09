package org.openeel.shared.domain.report.formatter

import org.openeel.shared.resources.StringUiText
import org.openeel.shared.resources.UiText

/**
 * Base formatter for count values (simple numeric display)
 */
class CountGraphFormatter : GraphFormatter<Double> {
    override fun adjust(value: Double): Double = value

    override fun format(value: Double): UiText {
        return StringUiText(value.toInt().toString())
    }
}