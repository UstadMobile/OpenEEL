package org.openeel.app.view.report.indicator.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.openeel.app.components.defaultItemPadding
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.viewmodel.report.indictor.detail.IndicatorDetailUiState
import org.openeel.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel

@Composable
fun IndicatorDetailScreen(
    viewModel: IndicatorDetailViewModel
) {
    val uiState: IndicatorDetailUiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .defaultItemPadding(),
    ) {
        when {
            uiState.errorMessage != null -> Text(
                "Error: ${uiState.errorMessage}",
                color = MaterialTheme.colorScheme.error
            )

            else -> {
                Text(
                    text = uiState.indicator.dataOrNull()?.name ?: "",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = uiState.indicator.dataOrNull()?.description ?: "",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
        }
    }
}