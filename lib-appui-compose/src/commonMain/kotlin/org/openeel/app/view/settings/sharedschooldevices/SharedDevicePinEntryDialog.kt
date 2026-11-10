package org.openeel.app.view.settings.sharedschooldevices

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.cancel
import org.openeel.shared.generated.resources.save
import org.openeel.shared.generated.resources.set_pin
import org.openeel.shared.resources.UiText

@Composable
fun SharedDevicePinEntryDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    errorMessage: UiText? = null,
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    var currentPin by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(Res.string.set_pin))
        },
        text = {
            OutlinedTextField(
                value = currentPin,
                onValueChange = { newPin ->
                    currentPin = newPin
                },
                modifier = Modifier
                    .testTag("pin_text")
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                singleLine = true,
                isError = errorMessage != null,
                supportingText = errorMessage?.let {
                    { Text(uiTextStringResource(it)) }
                },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.NumberPassword
                ),
            )
        },
        confirmButton = {
            TextButton(onClick = { onSave(currentPin) }) {
                Text(stringResource(Res.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(Res.string.cancel))
            }
        },
    )
}
