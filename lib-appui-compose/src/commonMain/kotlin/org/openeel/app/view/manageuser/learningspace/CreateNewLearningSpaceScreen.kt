package org.openeel.app.view.manageuser.learningspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_from_link
import org.openeel.app.app.OpenEelAsyncImage
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateNewLearningSpaceUiState
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateNewLearningSpaceViewModel

@Composable
fun CreateNewLearningSpaceScreen(
    viewModel: CreateNewLearningSpaceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateNewLearningSpaceScreen(
        uiState = uiState,
        onClickAddLink = viewModel::onClickAddLink,
        onSelectHost = viewModel::onSelectHost,
    )
}

@Composable
fun CreateNewLearningSpaceScreen(
    uiState: CreateNewLearningSpaceUiState,
    onClickAddLink: () -> Unit,
    onSelectHost: (SchoolDirectory) -> Unit,
) {
    val hostsState = uiState.hosts

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "add_from_link") {
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClickAddLink() },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = null,
                    )
                },
                headlineContent = {
                    Text(text = stringResource(Res.string.add_from_link))
                }
            )
        }

        items(
            items = hostsState.dataOrNull().orEmpty(),
            key = { it.baseUrl.toString() },
        ) { host ->
            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onSelectHost(host) })
                    .testTag("host_item"),
                leadingContent = {
                    OpenEelAsyncImage(
                        uri = host.icon,
                        contentDescription = "",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(24.dp),
                    )
                },
                headlineContent = {
                    Text(
                        text = host.baseUrl.toString()
                    )
                },
            )

        }
    }
}
