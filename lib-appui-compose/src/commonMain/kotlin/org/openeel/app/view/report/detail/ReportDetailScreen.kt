package world.respect.app.view.report.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.view.report.graph.CombinedGraph
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.empty_data
import world.respect.shared.viewmodel.report.detail.ReportDetailUiState
import world.respect.shared.viewmodel.report.detail.ReportDetailViewModel

@Composable
fun ReportDetailScreen(
    navController: NavHostController,
    viewModel: ReportDetailViewModel
) {
    val uiState: ReportDetailUiState by viewModel.uiState.collectAsStateWithLifecycle(
        initialValue = ReportDetailUiState(),
        context = Dispatchers.Main.immediate
    )
    ReportDetailScreen(
        uiState = uiState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailScreen(
    uiState: ReportDetailUiState
) {
    Column(Modifier.fillMaxSize()) {
        if (uiState.reportResult?.results?.isNotEmpty() == true) {
            val reportResult = uiState.reportResult
            if (reportResult != null) {
                CombinedGraph(
                    reportResult = reportResult,
                    modifier = Modifier.weight(0.6f),
                    xAxisFormatter = uiState.xAxisFormatter,
                    yAxisFormatter = uiState.yAxisFormatter
                )
            }
        } else {
            EmptyDataMessage(Modifier.weight(1f))
        }
    }
}


@Composable
private fun EmptyDataMessage(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(stringResource(Res.string.empty_data))
    }
}