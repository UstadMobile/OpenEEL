package world.respect.shared.domain.report.query

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import world.respect.lib.xapi.extensions.reportoptions.StatementReportRow

class MockRunReportUseCaseClientImpl(): RunReportUseCase  {
    override fun invoke(request: RunReportUseCase.RunReportRequest): Flow<RunReportUseCase.RunReportResult> {
        return flowOf(
            RunReportUseCase.RunReportResult(
                results = listOf(
                    listOf(
                        StatementReportRow(120383.0, "2023-01-01", "2"),
                        StatementReportRow(183248.0, "2023-01-02", "1"),
                        StatementReportRow(9732.0, "2023-01-03", "1"),
                        StatementReportRow(2187324.0, "2023-01-04", "2"),
                        StatementReportRow(187423.0, "2023-01-05", "1"),
                        StatementReportRow(33033.0, "2023-01-06", "2"),
                        StatementReportRow(2362.0, "2023-01-07", "1")
                    ),
                    // Second series data
                    listOf(
                        StatementReportRow(6324.0, "2023-01-01", "1"),
                        StatementReportRow(9730.0, "2023-01-02", "1"),
                        StatementReportRow(43325.0, "2023-01-03", "2"),
                        StatementReportRow(18325.0, "2023-01-04", "1"),
                        StatementReportRow(753874.0, "2023-01-05", "2"),
                        StatementReportRow(03847.0, "2023-01-06", "2"),
                        StatementReportRow(023783.0, "2023-01-07", "2")
                    )
                ),
                timestamp = System.currentTimeMillis(),
                request = request,
                age = 0
            )
        )
    }
}