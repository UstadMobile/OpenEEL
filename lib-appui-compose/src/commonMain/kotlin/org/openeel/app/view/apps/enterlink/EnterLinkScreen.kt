package world.respect.app.view.apps.enterlink

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectShortVersionInfoText
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.*
import world.respect.app.components.uiTextStringResource
import world.respect.shared.util.ext.isLoading
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkUiState
import world.respect.shared.viewmodel.apps.enterlink.EnterLinkViewModel

@Composable
fun EnterLinkScreen(
    viewModel: EnterLinkViewModel
) {
    val uiState by viewModel.uiState.collectAsState(
        EnterLinkUiState(), Dispatchers.Main.immediate
    )

    val appUiState by viewModel.appUiState.collectAsState()

    EnterLinkScreen(
        uiState = uiState,
        appUiState = appUiState,
        onLinkChanged = viewModel::onLinkChanged,
        onClickNext = viewModel::onClickNext,
    )
}

@Composable
fun EnterLinkScreen(
    uiState: EnterLinkUiState,
    appUiState: AppUiState,
    onLinkChanged: (String) -> Unit,
    onClickNext: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.app_link_provided_message),
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.linkUrl,
                onValueChange = onLinkChanged,
                label = {
                    Text(
                        text = stringResource(Res.string.link_label)
                    )
                },
                placeholder = {
                    Text(
                        text = stringResource(Res.string.example_url_placeholder),
                    )
                },
                enabled = !appUiState.isLoading,
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.errorMessage != null,
                supportingText = uiState.errorMessage?.let {
                    {
                        Text(
                            text = uiTextStringResource(it)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onClickNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = !appUiState.isLoading,
            ) {
                Text(
                    text = stringResource(Res.string.next),
                )
            }
        }

        RespectShortVersionInfoText(
            Modifier.defaultItemPadding().fillMaxWidth().align(Alignment.BottomCenter)
        )
    }

}
