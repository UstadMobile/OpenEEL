package org.openeel.app.view.manageuser.learningspace

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.registration_url_label
import org.openeel.app.components.OpenEelEnterLinkColumn
import org.openeel.app.components.OpenEelShortVersionInfoText
import org.openeel.app.components.defaultItemPadding
import org.openeel.shared.util.ext.isLoading
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.manageuser.learningspace.EnterHostLinkUiState
import org.openeel.shared.viewmodel.manageuser.learningspace.EnterHostLinkViewModel

@Composable
fun EnterHostLinkScreen(viewModel: EnterHostLinkViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    EnterHostLinkScreen(
        uiState = uiState,
        appUiState = appUiState,
        onLinkChanged = viewModel::onLinkChanged,
        onClickNext = viewModel::onClickNext,
    )
}

@Composable
fun EnterHostLinkScreen(
    uiState: EnterHostLinkUiState,
    appUiState: AppUiState,
    onLinkChanged: (String) -> Unit,
    onClickNext: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        OpenEelEnterLinkColumn(
            linkUrl = uiState.linkUrl,
            errorMessage = uiState.errorMessage,
            onLinkChanged = onLinkChanged,
            onClickNext = onClickNext,
            enabled = !appUiState.isLoading,
            label = stringResource(Res.string.registration_url_label),
            modifier = Modifier.fillMaxWidth().padding(16.dp),
        )

        OpenEelShortVersionInfoText(
            Modifier.defaultItemPadding().fillMaxWidth().align(Alignment.BottomCenter)
        )
    }
}
