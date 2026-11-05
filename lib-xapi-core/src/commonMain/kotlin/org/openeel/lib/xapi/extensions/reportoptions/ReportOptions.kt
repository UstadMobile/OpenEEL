package org.openeel.lib.xapi.extensions.reportoptions

import kotlinx.serialization.Serializable

/**
 * Represents options selected by the user to generate a report.
 */
@Serializable
data class ReportOptions(
    val title: String = "",
    val xAxis: ReportXAxis = ReportXAxis.DAY,
    val period: ReportPeriod = ReportPeriodOption.LAST_WEEK.period,
    val series: List<ReportSeries> = emptyList(),
)