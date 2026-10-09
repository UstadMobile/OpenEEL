package world.respect.app.view.report.indicator.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import world.respect.app.components.defaultItemPadding
import world.respect.lib.xapi.extensions.reportoptions.Indicator
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListUiState
import world.respect.shared.viewmodel.report.indictor.list.IndicatorListViewModel

@Composable
fun IndicatorListScreen(
    viewModel: IndicatorListViewModel
) {
    val uiState: IndicatorListUiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        when {
            uiState.errorMessage != null -> {
                Text(
                    text = "Error: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )
            }

            else -> {
                IndicatorListContent(
                    indicators = uiState.indicators,
                    onItemClick = { indicator ->
                        viewModel.onIndicatorSelected(indicator)
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

@Composable
private fun IndicatorListContent(
    indicators: List<Indicator>,
    onItemClick: (Indicator) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
    ) {
        items(indicators, key = { it.name }) { indicator ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onItemClick(indicator) }
                    .padding(vertical = 12.dp)
            ) {
                Text(
                    text = indicator.name,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = indicator.description,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}