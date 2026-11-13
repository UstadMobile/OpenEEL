package org.openeel.app.view.manageuser.learningspace

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.learn_more
import org.openeel.shared.generated.resources.registration_url_info
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
        onClickLearnMore = viewModel::onClickLearnMore,
    )
}

@Composable
fun EnterHostLinkScreen(
    uiState: EnterHostLinkUiState,
    appUiState: AppUiState,
    onLinkChanged: (String) -> Unit,
    onClickNext: () -> Unit,
    onClickLearnMore: () -> Unit,
) {
    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            OpenEelEnterLinkColumn(
                linkUrl = uiState.linkUrl,
                errorMessage = uiState.errorMessage,
                onLinkChanged = onLinkChanged,
                onClickNext = onClickNext,
                enabled = !appUiState.isLoading,
                label = stringResource(Res.string.registration_url_label),
                modifier = Modifier.fillMaxWidth(),
            )

            OutlinedCard(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                Row(
                    modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Info,
                        contentDescription = null,
                    )
                    Text(
                        text = stringResource(Res.string.registration_url_info),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }

                TextButton(
                    onClick = onClickLearnMore,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp),
                ) {
                    Text(stringResource(Res.string.learn_more))
                }
            }
        }

        OpenEelShortVersionInfoText(
            Modifier.defaultItemPadding().fillMaxWidth().align(Alignment.BottomCenter)
        )
    }
}
