package org.openeel.app.view.report.indicator.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.openeel.app.components.defaultItemPadding
import org.openeel.lib.xapi.extensions.reportoptions.Indicator
import org.openeel.shared.viewmodel.report.indictor.list.IndicatorListUiState
import org.openeel.shared.viewmodel.report.indictor.list.IndicatorListViewModel

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
            ListItem(
                modifier = Modifier
                    .clickable { onItemClick(indicator) },
                headlineContent = {
                    Text(text = indicator.name)
                },
                supportingContent = {
                    Text(
                        text = indicator.description,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
            )
        }
    }
}