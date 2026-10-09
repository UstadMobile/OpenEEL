package world.respect.app.view.report.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import world.respect.shared.generated.resources.blank_template
import world.respect.shared.viewmodel.report.list.ReportTemplateListUiState
import world.respect.shared.viewmodel.report.list.ReportTemplateListViewModel

@Composable
fun ReportTemplateListScreen(
    viewModel: ReportTemplateListViewModel
) {
    val uiState: ReportTemplateListUiState by viewModel.uiState.collectAsState(
        ReportTemplateListUiState()
    )
    LazyColumn {
        item {
            ReportTemplateCard(
                report = Report(
                    guid = "0",
                    title = stringResource(Res.string.blank_template),
                    reportOptions = ReportOptions(),
                    reportIsTemplate = true,
                    ownerGuid = ""
                ),
                viewModel = viewModel,
                activeUserPersonUid = uiState.activeUserPersonUid
            )
        }
        items(uiState.templates.dataOrNull() ?: emptyList()) { report ->
            ReportTemplateCard(
                report = report,
                viewModel = viewModel,
                activeUserPersonUid = uiState.activeUserPersonUid
            )
        }
    }
}

@Composable
private fun ReportTemplateCard(
    report: Report,
    viewModel: ReportTemplateListViewModel,
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

    Row(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .height(100.dp)  // Fixed height for consistency
            .clickable { viewModel.onTemplateSelected(report) },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxHeight(),
            contentAlignment = Alignment.CenterStart
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
                    isSmallSize = true,
                    reportResult = reportResultWithFormatters.reportResult,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.surface),
                    xAxisFormatter = reportResultWithFormatters.xAxisFormatter,
                    yAxisFormatter = reportResultWithFormatters.yAxisFormatter
                )
            }
        }
        Spacer(modifier = Modifier.width(8.dp))
        // Title above the chart
        Text(
            text = report.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp).weight(1f)
                .weight(2f),
        )
    }
}