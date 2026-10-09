package world.respect.app.view.manageuser.createaccount

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.how_passkey_works
import world.respect.shared.generated.resources.next
import world.respect.shared.generated.resources.other_way_to_sign_in
import world.respect.shared.generated.resources.passkey_description
import world.respect.shared.generated.resources.required
import world.respect.shared.generated.resources.sign_up
import world.respect.shared.generated.resources.signing_in
import world.respect.shared.generated.resources.username_label
import world.respect.shared.resources.StringResourceUiText
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModel
import world.respect.shared.viewmodel.manageuser.signup.CreateAccountViewModelUiState

@Composable
fun CreateAccountScreen(viewModel: CreateAccountViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    CreateAccountScreen(
        uiState = uiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onClickSignupWithPasskey = viewModel::onClickSignupWithPasskey,
        onClickOtherOptions = viewModel::onOtherOptionsClick,
        onClickHowPasskeysWork = viewModel::onClickHowPasskeysWork
    )
}

@Composable
fun CreateAccountScreen(
    uiState: CreateAccountViewModelUiState,
    onUsernameChanged: (String) -> Unit,
    onClickSignupWithPasskey: () -> Unit,
    onClickOtherOptions: () -> Unit,
    onClickHowPasskeysWork: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        uiState.signupError?.let {
            Text(uiTextStringResource(it))
        }

        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            enabled = uiState.fieldsEnabled,
            label = { Text(stringResource(Res.string.username_label) + "*") },
            placeholder = { Text(stringResource(Res.string.username_label)) },
            singleLine = true,
            isError = uiState.usernameError != null,
            supportingText = {
                Text(
                    text = uiTextStringResource(
                        uiState.usernameError ?: StringResourceUiText(Res.string.required)
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onKeyEvent { keyEvent ->
                    if (keyEvent.type == KeyEventType.KeyDown) {
                        !ValidateUsernameUseCase.isValidUsernameChar(
                            keyEvent.utf16CodePoint.toChar()
                        )
                    } else false
                }
        )
        if (uiState.passkeySupported) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(Res.string.signing_in),
            )
            Spacer(modifier = Modifier.height(4.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .padding(8.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append(stringResource(Res.string.passkey_description))
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold
                            )
                        ) {
                            append(stringResource(Res.string.how_passkey_works))
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickHowPasskeysWork() }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = stringResource(Res.string.other_way_to_sign_in),
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.clickable { onClickOtherOptions() }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickSignupWithPasskey,
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState.fieldsEnabled
        ) {
            Text(
                if (uiState.passkeySupported) {
                    stringResource(Res.string.sign_up)
                }
                else {
                    stringResource(Res.string.next)
                }
            )
        }

        uiState.generalError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(uiTextStringResource(it))
        }
    }
}
