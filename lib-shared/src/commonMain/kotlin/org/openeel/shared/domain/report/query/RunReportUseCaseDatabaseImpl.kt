package world.respect.shared.domain.report.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.TimeZone

/**
 *
 */
class RunReportUseCaseDatabaseImpl(
) : RunReportUseCase {


    /**
     * Run the report on the database as per the request
     *
     * @return a single value flow with the report result.
     */
    override fun invoke(
        request: RunReportUseCase.RunReportRequest,
    ): Flow<RunReportUseCase.RunReportResult> {
        val timeZone = TimeZone.of(request.timeZoneId)
        if (request.reportOptions.period.periodStartMillis(timeZone) >=
            request.reportOptions.period.periodEndMillis(timeZone)
        ) {
            throw IllegalArgumentException("Invalid time range: to time must be after from time")
        }

        return flow {

        }
    }
}