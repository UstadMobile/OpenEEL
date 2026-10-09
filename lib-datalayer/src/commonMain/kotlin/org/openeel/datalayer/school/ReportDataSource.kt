package world.respect.datalayer.school

import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.school.model.Report

interface ReportDataSource {

    /**
     * @param template if true, get list of templates. Otherwise list of reports for the active user
     */
   suspend fun allReportsAsFlow(
        template: Boolean
    ): Flow<DataLoadState<List<Report>>>

    suspend fun getReportAsync(loadParams: DataLoadParams, reportId: String): DataLoadState<Report>

    suspend fun getReportAsFlow(reportId: String): Flow<DataLoadState<Report>>

    suspend fun putReport(report: Report)

    suspend fun deleteReport(reportId: String)


}
