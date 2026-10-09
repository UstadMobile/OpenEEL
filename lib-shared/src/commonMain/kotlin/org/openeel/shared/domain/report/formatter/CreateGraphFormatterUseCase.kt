package world.respect.shared.domain.report.formatter

import world.respect.lib.xapi.extensions.reportoptions.ReportXAxis
import world.respect.lib.xapi.extensions.reportoptions.YAxisTypes
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText
import kotlin.reflect.KClass


class CreateGraphFormatterUseCase() {

    data class FormatterOptions<T : Any>(
        val paramType: KClass<T>,
        val axis: Axis,
        val forSubgroup: Boolean = false
    ) {
        enum class Axis {
            Y_AXIS_VALUES, X_AXIS_VALUES
        }
    }

    /**
     * Create a GraphFormatter for a given report result and axis (or other options)
     *
     * e.g. To get the y axis values:
     *
     * val yAxisValuesFormatter = invoke(
     *    reportResult = reportResult,
     *    optiosn = FormatterOptions(paramType = Double::class, axis = FormatterOptions.Axis.Y_AXIS_VALUES)
     * )
     *
     * If necessary this can use different implementations on different platforms
     */
    @Suppress("UNCHECKED_CAST") // Mike to check
    operator fun <T : Any> invoke(
        reportResult: RunReportUseCase.RunReportResult,
        options: FormatterOptions<T>
    ): GraphFormatter<T> {
        return when {
            options.axis == FormatterOptions.Axis.Y_AXIS_VALUES && options.paramType == Double::class -> {
                when (reportResult.request.reportOptions.series.first().reportSeriesYAxis.type) {
                    YAxisTypes.DURATION.name -> DurationGraphFormatter(reportResult)
                    else -> CountGraphFormatter()
                }
            }

            options.axis == FormatterOptions.Axis.X_AXIS_VALUES && options.paramType == String::class -> {
                // Use subgroup axis type if formatting for subgroup
                val axisType = if (options.forSubgroup) {
                    reportResult.request.reportOptions.series.first().reportSeriesSubGroup
                        ?: ReportXAxis.DAY
                } else {
                    reportResult.request.reportOptions.xAxis
                }
                when (axisType) {
                    ReportXAxis.DAY,
                    ReportXAxis.WEEK,
                    ReportXAxis.MONTH,
                    ReportXAxis.YEAR -> DateGraphFormatter(axisType)
                    ReportXAxis.GENDER -> GenderGraphFormatter()
                    else -> NoOpGraphFormatter()
                }
            }

            else -> {
                throw IllegalArgumentException("Unsupported formatter options combination")
            }
        } as GraphFormatter<T>
    }
}

/**
 * Formatter that returns values as-is without any transformation,
 * wrapping them in UiText for consistent localization handling.
 */
class NoOpGraphFormatter : GraphFormatter<String> {
    override fun adjust(value: String): String = value

    override fun format(value: String): UiText = StringUiText(value)
}