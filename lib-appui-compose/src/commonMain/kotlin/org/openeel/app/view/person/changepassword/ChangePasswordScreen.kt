package world.respect.app.view.person.changepassword

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
import world.respect.app.components.RespectPasswordField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.new_password
import world.respect.shared.generated.resources.old_password
import world.respect.shared.generated.resources.required
import world.respect.shared.util.ext.isLoading
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.person.changepassword.ChangePasswordUiState
import world.respect.shared.viewmodel.person.changepassword.ChangePasswordViewModel

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
