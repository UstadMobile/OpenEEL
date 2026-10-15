package org.openeel.app.view.apps.enterlink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.org.openeel.app.components.OpenEelEnterLinkColumn
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelShortVersionInfoText
import org.openeel.app.components.defaultItemPadding
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.app_link_provided_message
import org.openeel.shared.util.ext.isLoading
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.apps.enterlink.EnterLinkUiState
import org.openeel.shared.viewmodel.apps.enterlink.EnterLinkViewModel

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
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            Text(
                text = stringResource(Res.string.app_link_provided_message),
            )

            OpenEelEnterLinkColumn(
                linkUrl = uiState.linkUrl,
                errorMessage = uiState.errorMessage,
                onLinkChanged = onLinkChanged,
                onClickNext = onClickNext,
                enabled = !appUiState.isLoading,
                modifier = Modifier.fillMaxWidth(),
            )
        }

        OpenEelShortVersionInfoText(
            Modifier.defaultItemPadding().fillMaxWidth().align(Alignment.BottomCenter)
        )
    }
}
