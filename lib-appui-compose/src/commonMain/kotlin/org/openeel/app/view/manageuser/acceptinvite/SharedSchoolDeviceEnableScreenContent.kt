package org.openeel.app.view.manageuser.acceptinvite

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.device_name
import org.openeel.shared.generated.resources.required_field
import org.openeel.shared.viewmodel.manageuser.acceptinvite.AcceptInviteUiState


@Composable
fun SharedSchoolDeviceEnableScreenContent(
    uiState: AcceptInviteUiState,
    onDeviceNameChange: (String) -> Unit = {},
    onEnableSharedDeviceMode: () -> Unit = {},
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .defaultItemPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        uiState.sharedDeviceError?.let { error ->
            Text(
                text = uiTextStringResource(error),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth().testTag("shared_device_error")
            )
        }

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("device_name_input"),
            value = uiState.deviceName,
            label = { Text("${stringResource(Res.string.device_name)} *") },
            onValueChange = onDeviceNameChange,
            singleLine = true,
            isError = uiState.deviceNameError != null,
            supportingText = uiState.deviceNameError?.let {
                { Text(uiTextStringResource(it)) }
            },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Go),
            keyboardActions = KeyboardActions(
                onGo = {
                    onEnableSharedDeviceMode()
                }
            ),
        )

        SharedSchoolDeviceInfoBox(
            onClickEnableSharedSchoolDeviceMode = onEnableSharedDeviceMode
        )
    }
}