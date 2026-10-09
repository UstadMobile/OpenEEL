package world.respect.app.view.statement.list

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectEmptyListComponent
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.XapiStatement
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.no_matching_activity
import world.respect.shared.viewmodel.statement.list.StatementListUiState
import world.respect.shared.viewmodel.statement.list.StatementListViewModel

@Composable
fun StatementListScreen(
    viewModel: StatementListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    StatementListScreen(
        uiState = uiState,
        onClickListItem = viewModel::onClickListItem
    )
}

@Composable
fun StatementListScreen(
    uiState: StatementListUiState,
    onClickListItem: (statementId: XapiStatement) -> Unit = {},
) {
    val statements = uiState.statements.dataOrNull() ?: emptyList()
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(
            items = statements,
            key = { statement ->
                statement.id.toString()
            }
        ) { statement ->
            StatementListItem(
                statement = statement,
                onClickListItem = onClickListItem
            )
        }

        if (statements.isEmpty() ) {
            item("emptyitem") {
                RespectEmptyListComponent(
                    modifier = Modifier.fillMaxWidth().defaultItemPadding(),
                    text = stringResource(Res.string.no_matching_activity)
                )
            }
        }
    }
}