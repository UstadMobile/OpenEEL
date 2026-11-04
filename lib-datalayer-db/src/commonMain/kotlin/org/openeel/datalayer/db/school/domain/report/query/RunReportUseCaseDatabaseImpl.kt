package org.openeel.datalayer.db.school.domain.report.query

import androidx.room.RoomRawQuery
import com.ustadmobile.libcache.util.withWriterTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.domain.report.ext.age
import org.openeel.lib.xapi.extensions.reportoptions.ReportXAxis
import org.openeel.libutil.util.time.systemTimeInMillis

/**
 * Implementation of RunReportUseCase that runs queries against the local database.
 */
class RunReportUseCaseDatabaseImpl(
    private val schoolDatabase: RespectSchoolDatabase,
    private val generateReportQueriesUseCase: GenerateReportQueriesUseCase
) : RunReportUseCase {


    /**
     * Run the report on the database as per the request
     *
     * @return a single value flow with the report result.
     */
    override fun invoke(
        request: RunReportUseCase.RunReportRequest,
    ): Flow<RunReportUseCase.RunReportResult> {
        if (request.reportOptions.period.periodStartMillis(request.timeZone) >=
            request.reportOptions.period.periodEndMillis(request.timeZone)
        ) {
            throw IllegalArgumentException("Invalid time range: to time must be after from time")
        }

        return flow {
            val queries = generateReportQueriesUseCase(request = request)
            val queryResults = schoolDatabase.withWriterTransaction {
                val lastResultIsFresh = schoolDatabase.reportQueryResultDao().isReportFresh(
                    reportUid = request.reportUid,
                    timeZone = request.timeZone.id,
                    freshThresholdTime = queries.first().timestamp - (request.maxFreshAge * 1000),
                )

                if (!lastResultIsFresh) {
                    schoolDatabase.reportQueryResultDao().deleteByReportUidAndTimeZone(
                        request.reportUid, request.timeZone.id,
                    )


                    queries.forEach { query ->
                        val roomRawQuery = RoomRawQuery(
                            sql = query.sql,
                        ) { statement ->
                            query.params.forEachIndexed { index, paramVal ->
                                val bindIndex = index + 1
                                when (paramVal) {
                                    is String -> statement.bindText(bindIndex, paramVal)
                                    is Long -> statement.bindLong(bindIndex, paramVal)
                                    is Int -> statement.bindLong(bindIndex, paramVal.toLong())
                                    is Double -> statement.bindDouble(bindIndex, paramVal)
                                    is Float -> statement.bindDouble(
                                        bindIndex,
                                        paramVal.toDouble()
                                    )

                                    is Boolean -> statement.bindLong(
                                        bindIndex,
                                        if (paramVal) 1L else 0L
                                    )

                                    is ByteArray -> statement.bindBlob(bindIndex, paramVal)
                                    is Short -> statement.bindLong(bindIndex, paramVal.toLong())
                                    is Byte -> statement.bindLong(bindIndex, paramVal.toLong())
                                    else -> statement.bindText(bindIndex, paramVal.toString())
                                }
                            }
                        }
                        schoolDatabase.reportQueryResultDao().executeRawQuery(roomRawQuery)
                    }
                }

                schoolDatabase.reportQueryResultDao().getAllByReportUidAndTimeZone(
                    request.reportUid, request.timeZone.id
                )
            }

            // ClazzUids need to be looked up from the database so the user will see the name, not
            // the uid string. Other XAXis formatting (eg. dates, gender, etc) is done client side
            val isClazzUids = request.reportOptions.xAxis == ReportXAxis.CLASS

            val allClazzUids: List<Long> = if (isClazzUids) {
                queryResults.mapNotNull { it.rqrXAxis.toLongOrNull() }.distinct()
            } else {
                emptyList()
            }

            //Map clazz uid longs to name as string
            val clazzNames: Map<Long, String> =
                schoolDatabase.getClassEntityDao().findClazzNamesByUids(allClazzUids).associateBy(
                    keySelector = { it.cGuid }
                ) { it.cTitle }

            emit(
                RunReportUseCase.RunReportResult(
                    timestamp = systemTimeInMillis(),
                    request = request,
                    results = RunReportUseCase.reportQueryResultsToResultStatementReportRows(
                        queryResults = queryResults,
                        request = request,
                        xAxisNameFn = { xAxis ->
                            if (isClazzUids)
                                clazzNames[xAxis.toLongOrNull()] ?: xAxis
                            else
                                xAxis
                        }
                    ),
                    age = queryResults.age(sinceTimestamp = queries.first().timestamp)
                )
            )
        }
    }
}
