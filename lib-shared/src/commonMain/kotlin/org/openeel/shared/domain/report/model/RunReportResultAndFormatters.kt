package world.respect.shared.domain.report.model

import world.respect.shared.domain.report.formatter.GraphFormatter
import world.respect.shared.domain.report.query.RunReportUseCase

data class RunReportResultAndFormatters(
    val reportResult: RunReportUseCase.RunReportResult,
    val xAxisFormatter: GraphFormatter<String>?,
    val yAxisFormatter: GraphFormatter<Double>?
)