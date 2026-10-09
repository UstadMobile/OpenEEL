package world.respect.app.view.manageuser.enterinvitecode

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.enter_invite_code_message
import world.respect.shared.generated.resources.invite_code_label
import world.respect.shared.generated.resources.next
import world.respect.shared.viewmodel.manageuser.enterinvitecode.EnterInviteCodeUiState
import world.respect.shared.viewmodel.manageuser.enterinvitecode.EnterInviteCodeViewModel

@Composable
fun EnterInviteCodeScreen(
    viewModel: EnterInviteCodeViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    EnterInviteCodeScreen(
        uiState = uiState,
        onCodeChanged = viewModel::onCodeChanged,
        onClickNext = viewModel::onClickNext
    )
}

@Composable
fun EnterInviteCodeScreen(
    uiState: EnterInviteCodeUiState,
    onCodeChanged: (String) -> Unit,
    onClickNext: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(Res.string.enter_invite_code_message),
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.inviteCode,
            onValueChange = onCodeChanged,
            label = {
                Text(text = stringResource(Res.string.invite_code_label))
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.invite_code_label),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { onClickNext() }
            ),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null,
            supportingText = uiState.errorMessage?.let {
                { Text(uiTextStringResource(it)) }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.next),
            )
        }
    }
}
