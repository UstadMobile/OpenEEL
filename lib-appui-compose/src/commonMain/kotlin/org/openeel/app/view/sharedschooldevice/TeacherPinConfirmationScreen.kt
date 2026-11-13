package org.openeel.app.view.sharedschooldevice

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelDataLoadHost
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.enter_school_device_pin
import org.openeel.shared.generated.resources.next
import org.openeel.shared.viewmodel.sharedschooldevice.TeacherPinConfirmationUiState
import org.openeel.shared.viewmodel.sharedschooldevice.TeacherPinConfirmationViewModel


@Composable
fun TeacherPinConfirmationScreen(
    viewModel: TeacherPinConfirmationViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(context = Dispatchers.Main.immediate)
    TeacherPinConfirmationScreen(
        uiState = uiState,
        onPinChanged = viewModel::onPinChanged,
        onClickNext = viewModel::onClickNext
    )
}

@Composable
fun TeacherPinConfirmationScreen(
    uiState: TeacherPinConfirmationUiState,
    onPinChanged: (String) -> Unit,
    onClickNext: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    OpenEelDataLoadHost(dataLoadState = uiState.settings) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .defaultItemPadding(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = uiState.pin,
                onValueChange = onPinChanged,
                label = {
                    Text(text = stringResource(Res.string.enter_school_device_pin))
                },
                placeholder = {
                    Text(text = stringResource(Res.string.enter_school_device_pin))
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    autoCorrectEnabled = false,
                    imeAction = ImeAction.Go,
                ),
                keyboardActions = KeyboardActions(
                    onGo = { onClickNext() }
                ),
                modifier = Modifier
                    .testTag("Enter school device PIN")
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                isError = uiState.errorMessage != null,
                supportingText = uiState.errorMessage?.let { errorMessage ->
                    { Text(uiTextStringResource(errorMessage)) }
                }
            )

            Button(
                onClick = onClickNext,
                enabled = uiState.buttonEnabled,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(Res.string.next))
            }
        }
    }
}