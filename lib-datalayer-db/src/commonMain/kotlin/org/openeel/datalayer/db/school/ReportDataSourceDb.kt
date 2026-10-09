package org.openeel.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toReportEntity
import org.openeel.datalayer.db.school.adapters.toRespectReport
import org.openeel.datalayer.school.ReportDataSourceLocal
import org.openeel.datalayer.school.model.Report

class ReportDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
) : ReportDataSourceLocal {

    override suspend fun updateLocal(
        list: List<Report>,
        forceOverwrite: Boolean
    ) {

    }

    override suspend fun findByUidList(uids: List<String>): List<Report> {
        TODO("Not yet implemented")
    }

    override suspend fun allReportsAsFlow(template: Boolean): Flow<DataLoadState<List<Report>>> {
        return schoolDb.getReportEntityDao().getAllReportsByTemplate(template)
            .map { reportEntities ->
                DataReadyState(reportEntities.map { it.toRespectReport() })
            }
    }

    override suspend fun getReportAsync(
        loadParams: DataLoadParams,
        reportId: String
    ): DataLoadState<Report> {
        val reportEntity = schoolDb.getReportEntityDao().getReportAsync(reportId)
        return if (reportEntity != null) {
            DataReadyState(reportEntity.toRespectReport())
        } else {
            NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
        }
    }

    override suspend fun getReportAsFlow(reportId: String): Flow<DataLoadState<Report>> {
        return schoolDb.getReportEntityDao().getReportAsFlow(reportId).map { reportEntity ->
            if (reportEntity != null) {
                DataReadyState(reportEntity.toRespectReport())
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun putReport(report: Report) {
        val reportEntity = report.toReportEntity()
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getReportEntityDao().putReport(reportEntity)
            }
        }
    }

    override suspend fun deleteReport(reportId: String) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                schoolDb.getReportEntityDao().deleteReportByUid(reportId)
            }
        }
    }


}