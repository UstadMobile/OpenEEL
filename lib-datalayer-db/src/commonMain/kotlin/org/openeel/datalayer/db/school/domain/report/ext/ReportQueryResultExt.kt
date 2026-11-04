package org.openeel.datalayer.db.school.domain.report.ext

import org.openeel.datalayer.db.shared.entities.ReportQueryResultEntity
import org.openeel.lib.xapi.extensions.reportoptions.StatementReportRow

fun ReportQueryResultEntity.asStatementReportRow() = StatementReportRow(
    xAxis = rqrXAxis,
    yAxis = rqrYAxis,
    subgroup = rqrSubgroup,
)

/**
 * Determine the age of the report query results
 *
 * @param sinceTimestamp the timestamp to compare against (ms since epoch)
 * @return the age (as per http) of the report (in seconds since timestamp)
 */
fun List<ReportQueryResultEntity>.age(sinceTimestamp: Long): Int {
    return (firstOrNull()?.rqrLastModified?.let {
        sinceTimestamp - it
    }?.toInt() ?: 0) / 1000
}
