package world.respect.app.view.report.indicator.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.viewmodel.report.indictor.detail.IndicatorDetailUiState
import world.respect.shared.viewmodel.report.indictor.detail.IndicatorDetailViewModel

@Composable
fun IndicatorDetailScreen(
    navController: NavHostController,
    viewModel: IndicatorDetailViewModel
) {
    val uiState: IndicatorDetailUiState by viewModel.uiState.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxSize()
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
                    fontWeight = FontWeight.Bold,
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