package org.openeel.shared.domain.report.formatter

import io.ktor.util.toLowerCasePreservingASCIIRules
import kotlinx.datetime.LocalDate
import org.openeel.lib.xapi.extensions.reportoptions.ReportXAxis
import org.openeel.shared.resources.StringUiText
import org.openeel.shared.resources.UiText

/**
 * Formatter for date values (handles different date groupings)
 */
class DateGraphFormatter(
    private val xAxisType: ReportXAxis
) : GraphFormatter<String> {

    override fun adjust(value: String): String {
        return value
    }

    override fun format(value: String): UiText {
        return try {
            when (xAxisType) {
                ReportXAxis.MONTH -> {
                    val date = LocalDate.parse(value)
                    val monthShortName = date.month.name.take(3).replaceFirstChar { it.lowercase() }.toLowerCasePreservingASCIIRules()
                    StringUiText("$monthShortName - ${date.year}")
                }

                ReportXAxis.YEAR -> StringUiText(LocalDate.parse(value).year.toString())
                else -> StringUiText(value)
            }
        } catch (e: Exception) {
            StringUiText(value)
        }
    }
}