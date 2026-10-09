package world.respect.app.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.report.graph.CombinedGraph
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.extensions.reportoptions.ReportOptions
import world.respect.datalayer.school.model.Report
import world.respect.shared.domain.report.model.RunReportResultAndFormatters
import world.respect.shared.domain.report.query.RunReportUseCase
import world.respect.shared.generated.resources.No_data_available
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete
import world.respect.shared.viewmodel.report.list.ReportListUiState
import world.respect.shared.viewmodel.report.list.ReportListViewModel

@Composable
fun ReportListScreen(
    viewModel: ReportListViewModel
) {
    val uiState: ReportListUiState by viewModel.uiState.collectAsState(ReportListUiState())
    LazyVerticalGrid(
        columns = GridCells.Adaptive(200.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(4.dp)
    ) {
        items(uiState.reportList.dataOrNull() ?: emptyList<Report>()) { report ->
            ReportGridCard(
                report = report,
                viewModel = viewModel,
                activeUserPersonUid = uiState.activeUserPersonUid
            )
        }
    }
}

@Composable
private fun ReportGridCard(
    report: Report,
    viewModel: ReportListViewModel,
    activeUserPersonUid: Long
) {
    val reportDataFlow = remember(report.guid) {
        viewModel.runReport(report)
    }
    val reportResultWithFormatters by reportDataFlow.collectAsState(
        initial = RunReportResultAndFormatters(
            reportResult = RunReportUseCase.RunReportResult(
                timestamp = 0,
                request = RunReportUseCase.RunReportRequest(
                    reportUid = report.guid.toLong(),
                    reportOptions = ReportOptions(),
                    accountPersonUid = activeUserPersonUid,
                    timeZoneId = TimeZone.currentSystemDefault().id,
                ),
                results = emptyList()
            ),
            xAxisFormatter = null,
            yAxisFormatter = null
        )
    )

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                viewModel.onClickEntry(report)
            }
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.12f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box {
            Column(
                modifier = Modifier
                    .padding(4.dp)
                    .fillMaxWidth()
            ) {
                // Title above the chart
                Text(
                    report.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Chart content
                Box(
                    modifier = Modifier
                        .height(200.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    if (reportResultWithFormatters.reportResult.results.isEmpty() ||
                        reportResultWithFormatters.reportResult.resultSeries.isEmpty()
                    ) {
                        Text(
                            stringResource(Res.string.No_data_available),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        CombinedGraph(
                            reportResult = reportResultWithFormatters.reportResult,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.surface),
                            xAxisFormatter = reportResultWithFormatters.xAxisFormatter,
                            yAxisFormatter = reportResultWithFormatters.yAxisFormatter
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Delete icon positioned in top-right corner
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = stringResource(Res.string.delete),
                modifier = Modifier
                    .size(32.dp)
                    .padding(8.dp)
                    .clickable { viewModel.onRemoveReport(report.guid) }
                    .align(Alignment.TopEnd)
            )
        }
    }
}