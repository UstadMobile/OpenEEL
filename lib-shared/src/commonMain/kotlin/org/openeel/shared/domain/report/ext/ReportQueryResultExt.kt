package world.respect.shared.domain.report.ext

import world.respect.datalayer.db.shared.entities.ReportQueryResult
import world.respect.lib.xapi.extensions.reportoptions.StatementReportRow

fun ReportQueryResult.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)
