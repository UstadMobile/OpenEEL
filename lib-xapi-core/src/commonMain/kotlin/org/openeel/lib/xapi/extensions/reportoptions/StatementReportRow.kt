package org.openeel.lib.xapi.extensions.reportoptions

import kotlinx.serialization.Serializable

@Serializable
data class StatementReportRow(
    val yAxis: Double = 0.toDouble(),
    val xAxis: String = "",
    val subgroup: String = "",
)
