package org.openeel.app.view.person.setusernameandpassword

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelPasswordField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.app.components.OpenEelQrBadgeInfoBox
import org.openeel.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.change_password
import org.openeel.shared.generated.resources.password_label
import org.openeel.shared.generated.resources.set_password
import org.openeel.shared.generated.resources.username_label
import org.openeel.shared.util.ext.isLoading
import org.openeel.shared.viewmodel.app.appstate.AppUiState
import org.openeel.shared.viewmodel.person.setusernameandpassword.CreateAccountSetUserNameUiState
import org.openeel.shared.viewmodel.person.setusernameandpassword.CreateAccountSetUserNameViewModel

@Composable
fun CreateAccountSetUsernameScreen(
    viewModel: CreateAccountSetUserNameViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    CreateAccountSetUsernameScreen(
        uiState = uiState,
        appUiState = appUiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onClickAssignQrCodeBadge = viewModel::onClickAssignQrCodeBadge,
        onClickSetPassword = viewModel::onClickSetPassword,
        onClickQrBadgeLearnMore = viewModel::onClickQrBadgeLearnMore,
        onPasswordChanged = viewModel::onPasswordChanged,
    )
}

@Composable
fun CreateAccountSetUsernameScreen(
    uiState: CreateAccountSetUserNameUiState,
    appUiState: AppUiState,
    onUsernameChanged: (String) -> Unit,
    onClickAssignQrCodeBadge: () -> Unit,
    onClickSetPassword: () -> Unit,
    onClickQrBadgeLearnMore: () -> Unit,
    onPasswordChanged: (String) -> Unit,
) {

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(Res.string.username_label)) },
            singleLine = true,
            isError = uiState.usernameErr != null,
            supportingText = uiState.usernameErr?.let {
                { Text(uiTextStringResource(it)) }
            },
            enabled = !appUiState.isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("username")
                .defaultItemPadding()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        !ValidateUsernameUseCase.isValidUsernameChar(
                            keyEvent.utf16CodePoint.toChar()
                        )
                    } else false
                },
        )

        if (uiState.showQrBadgeInfoBox) {
            OpenEelQrBadgeInfoBox(
                onClickQrBadgeLearnMore,
                onClickAssignQrCodeBadge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                isQrBadgeSet = uiState.isQrBadgeSet
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.showQrBadgeInfoBox) {
            Button(
                onClick = onClickSetPassword,
                enabled = !appUiState.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
            ) {
                Text(
                    if (uiState.isPasswordSet) {
                        stringResource(Res.string.change_password)
                    } else {
                        stringResource(Res.string.set_password)
                    }
                )
            }
        }else {
            OpenEelPasswordField(
                value = uiState.password,
                onValueChange = onPasswordChanged,
                label = { Text(stringResource(Res.string.password_label)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                isError = uiState.passwordErr != null,
                supportingText = uiState.passwordErr?.let {
                    { Text(uiTextStringResource(it)) }
                },
                enabled = !appUiState.isLoading,
                modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("password")
            )
        }
    }
}
