package org.openeel.shared.domain.report.model

import org.openeel.shared.domain.report.formatter.GraphFormatter
import org.openeel.shared.domain.report.query.RunReportUseCase

data class RunReportResultAndFormatters(
    val reportResult: RunReportUseCase.RunReportResult,
    val xAxisFormatter: GraphFormatter<String>?,
    val yAxisFormatter: GraphFormatter<Double>?
)