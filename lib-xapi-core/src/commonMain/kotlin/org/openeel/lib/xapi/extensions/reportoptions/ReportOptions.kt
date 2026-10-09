package world.respect.lib.xapi.extensions.reportoptions

import kotlinx.serialization.Serializable

/**
 * Represents options selected by the user to generate a report. This is serialized into JSON
 * which is saved as to a string field on the Report entity.
 */
@Serializable
data class ReportOptions(
    val title: String = "",
    val xAxis: ReportXAxis = ReportXAxis.DAY,
    val period: ReportPeriod = ReportPeriodOption.LAST_WEEK.period,
    val series: List<ReportSeries> = emptyList(),
)