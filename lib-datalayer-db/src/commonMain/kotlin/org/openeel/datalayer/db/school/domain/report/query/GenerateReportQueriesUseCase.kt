package org.openeel.datalayer.db.school.domain.report.query

import kotlinx.datetime.offsetAt
import org.openeel.lib.xapi.extensions.reportoptions.ReportXAxis
import org.openeel.libutil.util.time.systemTimeInMillis
import org.openeel.datalayer.ext.DatabaseType
import org.openeel.datalayer.db.school.domain.report.query.RunReportUseCase

/**
 * `GenerateReportQueriesUseCase`
 *
 * This class is responsible for generating the SQL queries required to populate a report
 * based on the provided [RunReportUseCase.RunReportRequest]. It handles different database types
 * (SQLite and Postgres) and various report options such as the x-axis (time-based or categorical),
 * y-axis (metrics), and sub-grouping. This is normally something like 'total usage duration by week'
 * (see ReportOptions2 for available xAxis/yAxis options).
 *
 * ReportOptions2 allows the user to select the YAxis, XAXis, subgrouping (if any), time period, etc.
 *
 * The SQL uses the GROUP BY clause to aggregate data according to XAxis and subgrouping (if any).
 * Once data is grouped, we can use SQL aggregate functions (e.g. SUM, AVERAGE, COUNT etc) to get the
 * yAxis corresponding with each xAxis/subgroup combination.
 *
 * The results of the query will be inserted into the ReportQueryResultEntity table which essentially
 * acts as a cache. Report queries can be long/complex; hence it is necessary to cache the results,
 * even on the server side.
 */
class GenerateReportQueriesUseCase {

    /**
     * @param sql The SQL to run (including ? placeholders for any parameters)
     * @param params The parameters values to use
     * @param timestamp timestamp - used for the current time (e.g. ReportQueryResultEntity.rqrLastModified)
     *        This may be needed as the basis for subsequent calculations; and is therefor included
     *        in the return value.
     */
    data class ReportQueryParts2(
        val sql: String,
        val params: Array<Any>,
        val timestamp: Long,
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ReportQueryParts2) return false

            if (timestamp != other.timestamp) return false
            if (sql != other.sql) return false
            if (!params.contentEquals(other.params)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = timestamp.hashCode()
            result = 31 * result + sql.hashCode()
            result = 31 * result + params.contentHashCode()
            return result
        }
    }

    private fun xAxisOrSubgroupExpression(
        field: ReportXAxis,
        dbType: Int,
        request: RunReportUseCase.RunReportRequest,
    ): String {
        val reportOptions = request.reportOptions
        val timeZone = request.timeZone

        /**
         * Database functions will convert ms since epoch into dates using the UTC timezone: we want
         * to get the date as per the RunReportRequest timezone. Therefor we add the timezone offset
         * such that the SQLite/Postgres functions will return the date as expected.
         *
         * This methodology doesn't handle daylight saving time changes within the report period.
         */
        val offsetMillis = timeZone.offsetAt(reportOptions.period.periodStartInstant(timeZone))
            .totalSeconds * 1000

        val timeFieldName = "(ResultSource.timestamp + $offsetMillis)"

        /*
         * strftime should be able to use %F to create an iso formatted date, unfortunately, this
         * doesn't work across all SQLite versions, so '%Y-%m-%d' is used instead
         *
         * See https://www.sqlite.org/lang_datefunc.html
         */
        return buildString {
            when (field) {
                ReportXAxis.DAY -> {
                    append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch')")
                }

                ReportXAxis.WEEK -> {
                    /* Group data as documented on ReportXAxis.WEEK - on SQLite this works
                     * by :
                     *  a) When the day of week for the timestamp matches
                     *     StartOfWeekCte.TimeRangeStartDayOfWeek, just format the timestamp date
                     *  b) if not, use the weekday modifier which will advance the date
                     *     forward to when the day of week will match, then adjust backwards
                     *     7 days, thus grouping data by week commencing date.
                     */
                    append(
                        """
                              (CASE strftime('%w', $timeFieldName/1000, 'unixepoch')
                                    WHEN (SELECT StartOfWeekCte.TimeRangeStartDayOfWeek
                                           FROM StartOfWeekCte)
                                	THEN strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch')
                                
                                	ELSE strftime('%Y-%m-%d', $timeFieldName/1000, 
                                                  'unixepoch', 
                                                  'weekday ' || 
                                                  (SELECT StartOfWeekCte.TimeRangeStartDayOfWeek
                                                     FROM StartOfWeekCte), 
                                                  '-7 day')
                                 END)
                            """.trimIndent()
                    )
                }

                ReportXAxis.MONTH -> {
                    append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch', 'start of month') ")

                }

                ReportXAxis.YEAR -> {

                    append("strftime('%Y-%m-%d', $timeFieldName/1000, 'unixepoch', 'start of year') ")
                }

                ReportXAxis.CLASS -> {
                    append("ResultSource.statementObjectUid1")
                }

                ReportXAxis.GENDER -> {
                    append("COALESCE(PersonEntity.pGender, 0)")
                }

                else -> {}
            }
        }
    }

    operator fun invoke(
        request: RunReportUseCase.RunReportRequest,
        dbType: Int = DatabaseType.getCurrentDbType(),
    ): List<ReportQueryParts2> {
        val reportOptions = request.reportOptions
        val xAxis = reportOptions.xAxis

        val reportFromMs = reportOptions.period.periodStartMillis(request.timeZone)
        val reportToMs = reportOptions.period.periodEndMillis(request.timeZone)
        val timenow = systemTimeInMillis()

        val offsetMillis = request.timeZone.offsetAt(reportOptions.period.periodStartInstant(request.timeZone))
            .totalSeconds * 1000

        return reportOptions.series.mapIndexed { index, series ->
            val yAxis = series.reportSeriesYAxis
            val paramsList = mutableListOf<Any>()

            val ctes = mutableListOf<String>()

            if (xAxis == ReportXAxis.WEEK || series.reportSeriesSubGroup == ReportXAxis.WEEK) {
                ctes.add("""
                StartOfWeekCte(TimeRangeStartDayOfWeek) AS (
                    SELECT strftime('%w', ?, 'unixepoch') AS TimeRangeStartDayOfWeek
                )
            """.trimIndent())
                paramsList.add((reportFromMs + offsetMillis) / 1000)
            }

            ctes.add("""
            ResultSourceCte AS (
                SELECT 
                    ${yAxis.sql} AS yAxis,
                    ${xAxisOrSubgroupExpression(xAxis, dbType, request)} AS xAxis,
                    ${
                when (series.reportSeriesSubGroup) {
                    null -> "'' AS subgroup"
                    else -> "${xAxisOrSubgroupExpression(series.reportSeriesSubGroup!!, dbType, request)} AS subgroup"
                }
            }
                FROM XapiStatementEntity ResultSource
                ${
                if (reportOptions.xAxis.personJoinRequired ||
                    series.reportSeriesSubGroup?.personJoinRequired == true
                ) {
                    """
                    JOIN XapiActorEntity ON XapiActorEntity.actorUid = ResultSource.statementActorUid
                    JOIN PersonEntity ON (PersonEntity.pGuid = XapiActorEntity.actorAccountName)
                    """.trimIndent()
                } else ""
            }
                WHERE ResultSource.timestamp BETWEEN ? AND ?
                GROUP BY xAxis${series.reportSeriesSubGroup?.let { ", subgroup" } ?: ""}
            )
        """.trimIndent())
            paramsList.add(reportFromMs)
            paramsList.add(reportToMs)

            val sql = """
            WITH ${ctes.joinToString(",\n")}
            INSERT INTO ReportQueryResultEntity(
                rqrReportUid, rqrLastModified, rqrReportSeriesUid, rqrLastValidated, 
                rqrXAxis, rqrYAxis, rqrSubgroup, rqrTimeZone
            )
            SELECT 
                ? AS rqrReportUid,
                ? AS rqrLastModified,
                ? AS rqrReportSeriesUid,
                ? AS rqrLastValidated,
                COALESCE(ResultSourceCte.xAxis, '') AS rqrXAxis,
                COALESCE(ResultSourceCte.yAxis, 0.0) AS rqrYAxis,
                COALESCE(ResultSourceCte.subgroup, '') AS rqrSubgroup,
                ? AS rqrTimeZone
            FROM ResultSourceCte
        """.trimIndent()

            paramsList.addAll(
                listOf(
                    request.reportUid,
                    timenow,
                    index,
                    timenow,
                    request.timeZone.id
                )
            )

            ReportQueryParts2(sql, paramsList.toTypedArray(), timenow)
        }
    }
}
