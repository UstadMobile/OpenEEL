package org.openeel.app.view.manageuser.waitingforapproval

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultScreenPadding
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.please_wait
import org.openeel.shared.generated.resources.refresh
import org.openeel.shared.generated.resources.waiting_for_approval_to_join
import org.openeel.shared.viewmodel.manageuser.waitingforapproval.WaitingForApprovalViewModel

@Composable
fun WaitingForApprovalScreen(viewModel: WaitingForApprovalViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    WaitingForApprovalScreen(
        className = uiState.className,
        isRefreshing = uiState.isRefreshing,
    )
}

@Composable
fun WaitingForApprovalScreen(
    className: String,
    isRefreshing: Boolean,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(Res.string.please_wait),
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = stringResource(Res.string.waiting_for_approval_to_join, className),
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (isRefreshing) {
            CircularProgressIndicator()
        } else {
            IconButton(onClick = {  }) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = stringResource(Res.string.refresh)
                )
            }
        }
    }
}
