package world.respect.app.view.schooldirectory.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.delete
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListUiState
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryListViewModel
import world.respect.shared.viewmodel.schooldirectory.list.SchoolDirectoryMode

@Composable
fun SchoolDirectoryListScreen(
    viewModel: SchoolDirectoryListViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    SchoolDirectoryListScreen(
        uiState = uiState,
        onDeleteClick = { viewModel.onDeleteDirectory(it) },
        onSelectClick = { viewModel.onSelectDirectory(it) }
    )
}

@Composable
fun SchoolDirectoryListScreen(
    uiState: SchoolDirectoryListUiState,
    onDeleteClick: (RespectSchoolDirectory) -> Unit,
    onSelectClick: (RespectSchoolDirectory) -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(uiState.schoolDirectory) { directory ->
            SchoolDirectoryListItem(
                directory = directory,
                mode = uiState.mode,
                onDeleteClick = { onDeleteClick(directory) },
                onSelectClick = { onSelectClick(directory) }
            )
        }
    }
}

@Composable
fun SchoolDirectoryListItem(
    directory: RespectSchoolDirectory,
    mode: SchoolDirectoryMode,
    onDeleteClick: () -> Unit,
    onSelectClick: () -> Unit
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = mode == SchoolDirectoryMode.SELECT) {
                if (mode == SchoolDirectoryMode.SELECT) {
                    onSelectClick()
                }
            },
        headlineContent = {
            Text(
                text = directory.baseUrl.toString(),
                modifier = Modifier.testTag("host_url")
            )
        },
        trailingContent = {
            if (mode == SchoolDirectoryMode.MANAGE) {
                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(Res.string.delete)
                    )
                }
            }
        }
    )
}