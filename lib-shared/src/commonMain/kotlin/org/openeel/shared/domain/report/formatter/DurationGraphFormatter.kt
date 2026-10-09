package world.respect.shared.domain.report.formatter

import world.respect.datalayer.ext.MS_PER_HOUR
import world.respect.datalayer.ext.MS_PER_MIN
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.resources.StringUiText
import world.respect.shared.resources.UiText
import kotlin.math.roundToInt
import kotlin.time.DurationUnit

class DurationGraphFormatter(
    private val result: RunReportUseCase.RunReportResult,
) : GraphFormatter<Double> {

    private val unit: DurationUnit by lazy {
        val maxVal = result.results.maxOfOrNull { list ->
            list.maxOfOrNull { it.yAxis } ?: 0.0
        } ?: 0.0

        if (maxVal > MS_PER_HOUR) {
            DurationUnit.HOURS
        } else {
            DurationUnit.MINUTES
        }
    }

    override fun adjust(value: Double): Double {
        return when (unit) {
            DurationUnit.MINUTES -> value / MS_PER_MIN
            DurationUnit.HOURS -> value / MS_PER_HOUR
            else -> throw IllegalStateException()
        }
    }

    override fun format(value: Double): UiText {
        val roundedValue = (value * 100).roundToInt() / 100.0
        return StringUiText("$roundedValue")
    }
}