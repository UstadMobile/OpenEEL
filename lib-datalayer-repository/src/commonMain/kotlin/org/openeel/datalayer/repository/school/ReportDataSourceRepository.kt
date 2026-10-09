package world.respect.datalayer.repository.realm

import kotlinx.coroutines.flow.Flow
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.school.ReportDataSource
import world.respect.datalayer.school.model.Report

class ReportDataSourceRepository(
    private val remote: ReportDataSource,
) : ReportDataSource {
    override suspend fun allReportsAsFlow(template: Boolean): Flow<DataLoadState<List<Report>>> {
        TODO("Not yet implemented")
    }

    override suspend fun getReportAsync(
        loadParams: DataLoadParams,
        reportId: String
    ): DataLoadState<Report> {
        TODO("Not yet implemented")
    }

    override suspend fun getReportAsFlow(reportId: String): Flow<DataLoadState<Report>> {
        TODO("Not yet implemented")
    }

    override suspend fun putReport(report: Report) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteReport(reportId: String) {
        TODO("Not yet implemented")
    }
}