package world.respect.app.view.catalog.opdsfeededitaddlink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
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
import world.respect.app.app.RespectAsyncImage
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_thumbnail
import world.respect.shared.generated.resources.description
import world.respect.shared.generated.resources.example_url_placeholder
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.title
import world.respect.shared.util.ext.asUiText
import world.respect.shared.viewmodel.catalog.opdsfeededitaddlink.OpdsFeedEditAddLinkUiState
import world.respect.shared.viewmodel.catalog.opdsfeededitaddlink.OpdsFeedEditAddLinkViewModel
@Composable
fun OpdsFeedEditAddLinkScreen(
    viewModel: OpdsFeedEditAddLinkViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    OpdsFeedEditAddLinkScreen(
        uiState = uiState,
        onUrlChanged = viewModel::onUrlChanged,
        onClickNext = viewModel::onClickNext,
        onTitleChanged = viewModel::onTitleChanged,
        onDescriptionChanged = viewModel::onDescriptionChanged,
    )
}
@Composable
fun OpdsFeedEditAddLinkScreen(
    uiState: OpdsFeedEditAddLinkUiState = OpdsFeedEditAddLinkUiState(),
    onUrlChanged: (String) -> Unit = {},
    onClickNext: () -> Unit = {},
    onTitleChanged: (String) -> Unit = {},
    onDescriptionChanged: (String) -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
            .verticalScroll(rememberScrollState()),
    ) {
        if (uiState.step == OpdsFeedEditAddLinkUiState.Step.URL) {
            OutlinedTextField(
                value = uiState.url,
                onValueChange = onUrlChanged,
                isError = uiState.urlError != null,
                supportingText = uiState.urlError?.let { error ->
                    { Text(text = uiTextStringResource(error)) }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("external_link_url_field"),
                singleLine = true,
                placeholder = { Text(text = stringResource(Res.string.example_url_placeholder)) }
            )
            Button(
                onClick = onClickNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("external_link_next_button"),
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = stringResource(Res.string.next))
                }
            }
        }
        if (uiState.step == OpdsFeedEditAddLinkUiState.Step.METADATA) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.imageUrl != null) {
                        RespectAsyncImage(
                            uri = uiState.imageUrl,
                            contentDescription = "",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(80.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Filled.Language,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                        )
                    }
                }
                
                Spacer(modifier = Modifier.size(12.dp))
                
                Text(
                    text = stringResource(Res.string.add_thumbnail),
                )
            }

            OutlinedTextField(
                value = uiState.title,
                onValueChange = onTitleChanged,
                label = { Text(stringResource(Res.string.title)) },
                isError = uiState.titleError != null,
                supportingText = {
                    Text(uiTextStringResource(uiState.titleError ?: Res.string.required.asUiText()))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("external_link_title_field"),
                singleLine = true,
            )
            OutlinedTextField(
                value = uiState.description,
                onValueChange = onDescriptionChanged,
                label = { Text(stringResource(Res.string.description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultItemPadding()
                    .testTag("external_link_description_field"),
                minLines = 2,
            )
        }
    }
}
