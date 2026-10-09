package org.openeel.datalayer.repository.realm

import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.ReportDataSource
import org.openeel.datalayer.school.model.Report

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