package org.openeel.shared.domain.report.ext

import org.openeel.datalayer.db.shared.entities.ReportQueryResult
import org.openeel.lib.xapi.extensions.reportoptions.StatementReportRow

fun ReportQueryResult.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)
