package world.respect.app.view.statement.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import kotlinx.serialization.json.Json
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.shared.viewmodel.statement.detail.RawStatementUiState
import world.respect.shared.viewmodel.statement.detail.RawStatementViewModel

@Composable
fun RawStatementScreen(
    viewModel: RawStatementViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    RawStatementScreen(
        uiState = uiState
    )
}

@Composable
fun RawStatementScreen(
    uiState: RawStatementUiState,
) {
    val statement = uiState.statement.dataOrNull()
    val prettyJson = remember {
        Json {
            prettyPrint = true
        }
    }

    val jsonString = remember(statement) {
        if (statement != null) {
            prettyJson.encodeToString(statement)
        } else {
            ""
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .defaultItemPadding()
    ) {
        SelectionContainer {
            Text(
                text = jsonString,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}
