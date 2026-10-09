package world.respect.app.view.assignment.list

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import world.respect.app.components.RespectEmptyListComponent
import world.respect.app.components.defaultItemPadding
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.lib.xapi.model.AssignmentSummary
import world.respect.shared.viewmodel.assignment.list.AssignmentListUiState
import world.respect.shared.viewmodel.assignment.list.AssignmentListViewModel


@Composable
fun AssignmentListScreen(
    viewModel: AssignmentListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    AssignmentListScreen(
        uiState = uiState,
        onClickAssignment = viewModel::onClickAssignment,
    )
}

@Composable
fun AssignmentListScreen(
    uiState: AssignmentListUiState,
    onClickAssignment: (AssignmentSummary) -> Unit = { },
) {
    val assignments = uiState.assignments.dataOrNull() ?: emptyList()

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(
                items = assignments,
            ) { summary ->
                AssignmentListItem(
                    summary = summary,
                    onClick = onClickAssignment,
                    learningUnitInfoFlow = uiState.learningUnitInfoFlow,
                )
            }

            when {
                uiState.assignments is DataLoadingState<*> -> {

                }

                assignments.isEmpty() -> {
                    item("emptyitem") {
                        RespectEmptyListComponent(
                            modifier = Modifier.fillMaxWidth().defaultItemPadding()
                        )
                    }
                }

                else -> {
                    //do nothing
                }
            }
        }
    }
}
