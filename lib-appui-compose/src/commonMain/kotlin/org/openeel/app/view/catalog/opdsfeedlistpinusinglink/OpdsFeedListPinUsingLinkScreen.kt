package org.openeel.app.view.catalog.opdsfeedlistpinusinglink

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ustadmobile.libuicompose.org.openeel.app.components.OpenEelEnterLinkColumn
import kotlinx.coroutines.Dispatchers
import org.openeel.app.components.OpenEelShortVersionInfoText
import org.openeel.app.components.defaultItemPadding
import org.openeel.shared.util.ext.isLoading
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.catalog.opdsfeedlistpinusinglink.OpdsFeedListPinUsingLinkUiState
import org.openeel.shared.viewmodel.catalog.opdsfeedlistpinusinglink.OpdsFeedListPinUsingLinkViewModel

@Composable
fun OpdsFeedListPinUsingLinkScreen(
    viewModel: OpdsFeedListPinUsingLinkViewModel
) {
    val uiState by viewModel.uiState.collectAsState(
        OpdsFeedListPinUsingLinkUiState(), Dispatchers.Main.immediate
    )

    val appUiState by viewModel.appUiState.collectAsState()

    OpdsFeedListPinUsingLinkScreen(
        uiState = uiState,
        appUiState = appUiState,
        onLinkChanged = viewModel::onLinkUrlChanged,
        onClickNext = viewModel::onClickPinCollection,
    )
}

@Composable
fun OpdsFeedListPinUsingLinkScreen(
    uiState: OpdsFeedListPinUsingLinkUiState,
    appUiState: AppUiState,
    onLinkChanged: (String) -> Unit,
    onClickNext: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.fillMaxSize().padding(16.dp)
        ) {
            OpenEelEnterLinkColumn(
                linkUrl = uiState.linkUrlStr,
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

