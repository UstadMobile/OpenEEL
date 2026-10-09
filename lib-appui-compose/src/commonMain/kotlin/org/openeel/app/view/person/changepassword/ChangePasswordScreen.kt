package org.openeel.app.view.person.changepassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.RespectPasswordField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.new_password
import org.openeel.shared.generated.resources.old_password
import org.openeel.shared.generated.resources.required
import org.openeel.shared.util.ext.isLoading
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.person.changepassword.ChangePasswordUiState
import org.openeel.shared.viewmodel.person.changepassword.ChangePasswordViewModel

@Composable
fun ChangePasswordScreen(
    viewModel: ChangePasswordViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)

    val appUiState by viewModel.appUiState.collectAsState()

    ChangePasswordScreen(
        uiState = uiState,
        appUiState = appUiState,
        onChangeOldPassword = viewModel::onChangeOldPassword,
        onChangeNewPassword = viewModel::onChangeNewPassword,
    )
}

@Composable
fun ChangePasswordScreen(
    uiState: ChangePasswordUiState,
    appUiState: AppUiState,
    onChangeOldPassword: (String) -> Unit,
    onChangeNewPassword: (String) -> Unit,
) {

    Column(Modifier.fillMaxWidth()) {
        if(uiState.requireOldPassword) {
            RespectPasswordField(
                modifier = Modifier.testTag("old_password")
                    .fillMaxWidth()
                    .defaultItemPadding(),
                value = uiState.oldPassword,
                onValueChange = onChangeOldPassword,
                enabled = !appUiState.isLoading,
                label = {
                    Text(stringResource(Res.string.old_password) + "*")
                },
                supportingText = {
                    Text(
                        uiState.oldPasswordError?.let { uiTextStringResource(it) }
                            ?: stringResource(Res.string.required)
                    )
                },
                isError = uiState.oldPasswordError != null,
            )
        }

        RespectPasswordField(
            modifier = Modifier.testTag("new_password")
                .fillMaxWidth()
                .defaultItemPadding(),
            value = uiState.newPassword,
            onValueChange = onChangeNewPassword,
            enabled = !appUiState.isLoading,
            label = {
                Text(stringResource(Res.string.new_password) + "*")
            },
            supportingText = {
                Text(
                    uiState.newPasswordError?.let { uiTextStringResource(it) }
                        ?: stringResource(Res.string.required)
                )
            },
            isError = uiState.newPasswordError != null,
        )
    }
}
