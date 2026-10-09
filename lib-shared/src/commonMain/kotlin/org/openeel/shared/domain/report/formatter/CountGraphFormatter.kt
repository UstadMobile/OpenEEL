package world.respect.shared.domain.report.formatter

import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText

/**
 * Base formatter for count values (simple numeric display)
 */
class CountGraphFormatter : GraphFormatter<Double> {
    override fun adjust(value: Double): Double = value

    override fun format(value: Double): UiText {
        return StringUiText(value.toInt().toString())
    }
}