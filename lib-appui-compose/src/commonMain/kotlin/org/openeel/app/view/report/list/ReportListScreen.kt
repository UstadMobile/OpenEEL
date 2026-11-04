package org.openeel.app.view.report.list

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.view.report.graph.CombinedGraph
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.app.components.langMapString
import org.openeel.lib.xapi.ext.objectActivityNameOrNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.shared.generated.resources.No_data_available
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.delete
import org.openeel.shared.viewmodel.report.list.ReportListUiState
import org.openeel.shared.viewmodel.report.list.ReportListViewModel

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
        items(uiState.reportRequests.dataOrNull() ?: emptyList()) { request ->
            ReportGridCard(
                request = request,
                uiState = uiState,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun ReportGridCard(
    request: XapiStatement,
    uiState: ReportListUiState,
    viewModel: ReportListViewModel
) {
    val requestId = request.id?.toString() ?: return
    val reportResult = uiState.reportResults[requestId]
    val xAxisFormatter = uiState.xAxisFormatters[requestId]
    val yAxisFormatter = uiState.yAxisFormatters[requestId]

    Card(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxWidth()
            .clickable {
                viewModel.onClickEntry(request)
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
                    text = request.objectActivityNameOrNull()?.let { langMapString(it) } ?: "",
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
                    if (reportResult == null ||
                        reportResult.results.isEmpty() ||
                        reportResult.resultSeries.isEmpty()
                    ) {
                        Text(
                            stringResource(Res.string.No_data_available),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        CombinedGraph(
                            reportResult = reportResult,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(color = MaterialTheme.colorScheme.surface),
                            xAxisFormatter = xAxisFormatter,
                            yAxisFormatter = yAxisFormatter
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
                    .clickable { viewModel.onRemoveReport(request) }
                    .align(Alignment.TopEnd)
            )
        }
    }
}