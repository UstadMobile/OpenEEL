package org.openeel.app.view.manageuser.learningspace

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.shared.generated.resources.not_found
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.shared.util.exception.getUiTextOrGeneric
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_from_link
import org.openeel.app.components.uiTextStringResource
import org.openeel.app.app.OpenEelAsyncImage
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateLearningSpaceUiState
import org.openeel.shared.viewmodel.manageuser.learningspace.CreateLearningSpaceViewModel

@Composable
fun CreateLearningSpaceScreen(
    viewModel: CreateLearningSpaceViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    CreateLearningSpaceScreen(
        uiState = uiState,
        onClickAddLink = viewModel::onClickAddLink,
        onSelectHost = viewModel::onSelectHost,
    )
}

@Composable
fun CreateLearningSpaceScreen(
    uiState: CreateLearningSpaceUiState,
    onClickAddLink: () -> Unit,
    onSelectHost: (SchoolDirectory) -> Unit,
) {
    val hostsState = uiState.hosts
    val loadState = hostsState.let {
        if (it is NoDataLoadedState<*>) it.remoteState ?: it else it
    }

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

        when {
            hostsState is DataReadyState -> {
                items(
                    items = hostsState.data,
                    key = { it.baseUrl.toString() },
                ) { host ->
                    HostListItem(
                        host = host,
                        onClick = { onSelectHost(host) },
                    )
                }
            }

            loadState is DataErrorResult -> {
                item(key = "error") {
                    ListItem(
                        headlineContent = {
                            Text(uiTextStringResource(loadState.error.getUiTextOrGeneric()))
                        }
                    )
                }
            }

            loadState is NoDataLoadedState<*> -> {
                item(key = "not_found") {
                    ListItem(
                        headlineContent = { Text(stringResource(Res.string.not_found)) }
                    )
                }
            }

            else -> {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}

@Composable
fun HostListItem(
    host: SchoolDirectory,
    onClick: () -> Unit,
) {
    ListItem(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("host_item"),
        leadingContent = {
            OpenEelAsyncImage(
                uri = requireNotNull(host.icon) {
                    "Host ${host.baseUrl} must have an icon URL"
                }.toString(),
                contentDescription = "",
                contentScale = ContentScale.Fit,
                modifier = Modifier.size(24.dp),
            )
        },
        headlineContent = {
            Text(
                text = requireNotNull(host.name) {
                    "Host ${host.baseUrl} must have a name"
                }
            )
        },
        supportingContent = {
            Text(text = host.baseUrl.toString())
        },
    )
}
