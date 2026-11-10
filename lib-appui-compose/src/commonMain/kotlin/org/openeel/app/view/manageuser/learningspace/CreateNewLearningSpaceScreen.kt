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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import org.koin.compose.koinInject
import org.openeel.domain.getfavicons.GetFavIconUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_from_link
import org.openeel.app.app.OpenEelAsyncImage
import org.openeel.shared.generated.resources.enter_link_manually
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
    onSelectHost: (CreateNewLearningSpaceViewModel.Host) -> Unit,
) {
    val getFavIconUseCase: GetFavIconUseCase = koinInject()

    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item(key = "enter_link_manually") {
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
                    Text(text = stringResource(Res.string.enter_link_manually))
                }
            )
        }

        items(
            items = uiState.hosts,
            key = { it.baseUrl.toString() },
        ) { host ->
            var favIconUrl by remember(host.baseUrl) { mutableStateOf<String?>(null) }

            LaunchedEffect(host.baseUrl) {
                try {
                    favIconUrl = getFavIconUseCase(host.baseUrl).first().url
                } catch (e: Exception) {
                    Napier.w("Could not load favicon for ${host.baseUrl}", e)
                }
            }

            ListItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = { onSelectHost(host) })
                    .testTag("host_item"),
                leadingContent = {
                    OpenEelAsyncImage(
                        uri = favIconUrl,
                        contentDescription = "",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(24.dp),
                    )
                },
                headlineContent = {
                    Text(text = host.name)
                },
                supportingContent = {
                    Text(text = host.baseUrl.toString())
                },
            )

        }
    }
}
