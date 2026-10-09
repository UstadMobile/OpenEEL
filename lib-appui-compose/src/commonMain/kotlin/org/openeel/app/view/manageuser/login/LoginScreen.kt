package world.respect.app.view.manageuser.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPasswordField
import world.respect.app.components.RespectShortVersionInfoText
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.defaultScreenPadding
import world.respect.app.components.rememberCountryFlagEmoji
import world.respect.app.components.uiTextStringResource
import world.respect.shared.domain.account.username.validateusername.ValidateUsernameUseCase
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.i_have_an_invite_code
import world.respect.shared.generated.resources.select_another_school
import world.respect.shared.generated.resources.login
import world.respect.shared.generated.resources.password_label
import world.respect.shared.generated.resources.school_server_url
import world.respect.shared.generated.resources.server_location
import world.respect.shared.generated.resources.username_label
import world.respect.shared.util.ext.isLoading
import world.respect.shared.viewmodel.app.appstate.AppUiState
import world.respect.shared.viewmodel.manageuser.login.LoginUiState
import world.respect.shared.viewmodel.manageuser.login.LoginViewModel

@Composable
fun LoginScreen(
    viewModel: LoginViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    LoginScreen(
        uiState = uiState,
        appUiState = appUiState,
        onUsernameChanged = viewModel::onUsernameChanged,
        onPasswordChanged = viewModel::onPasswordChanged,
        onClickLogin = viewModel::onClickLogin,
        onClickInviteCode = viewModel::onClickInviteCode,
        onClickSelectAnotherSchool = viewModel::onClickSelectAnotherSchool
    )
}

@Composable
fun LoginScreen(
    uiState: LoginUiState,
    appUiState: AppUiState,
    onUsernameChanged: (String) -> Unit,
    onPasswordChanged: (String) -> Unit,
    onClickLogin: () -> Unit,
    onClickInviteCode: () -> Unit = {},
    onClickSelectAnotherSchool:() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultScreenPadding()
    ) {
        OutlinedTextField(
            value = uiState.username,
            onValueChange = onUsernameChanged,
            label = { Text(stringResource(Res.string.username_label)) },
            singleLine = true,
            isError = uiState.usernameError != null,
            supportingText = uiState.usernameError?.let {
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
                }
        )

        RespectPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.password_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            isError = uiState.passwordError != null,
            supportingText = uiState.passwordError?.let {
                { Text(uiTextStringResource(it)) }
            },
            enabled = !appUiState.isLoading,
            modifier = Modifier.fillMaxWidth().defaultItemPadding().testTag("password")
        )

        Button(
            onClick = onClickLogin,
            modifier = Modifier.fillMaxWidth().defaultItemPadding(),
            enabled = !appUiState.isLoading,
        ) {
            Text(text = stringResource(Res.string.login))
        }
        if (uiState.isSharedDevice) {
            OutlinedButton(
                onClick = onClickSelectAnotherSchool,
                modifier = Modifier.fillMaxWidth().defaultItemPadding()
            ) {
                Text(text = stringResource(Res.string.select_another_school))
            }
        }

        OutlinedButton(
            onClick = onClickInviteCode,
            modifier = Modifier.fillMaxWidth().defaultItemPadding()
        ) {
            Text(text = stringResource(Res.string.i_have_an_invite_code))
        }

        uiState.errorText?.also {
            Text(
                uiTextStringResource(it),
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.defaultItemPadding(),
            )
        }

        Text(
            "${stringResource(Res.string.school_server_url)}: ${uiState.schoolUrl}",
            modifier = Modifier.align(Alignment.CenterHorizontally).defaultItemPadding(),
            style = MaterialTheme.typography.bodySmall,
            textAlign = TextAlign.Center,
        )

        val countryFlagEmoji = rememberCountryFlagEmoji(uiState.schoolUrl)
        countryFlagEmoji?.takeIf { it.isNotEmpty() }?.also {
            Text(
                "${stringResource(Res.string.server_location)}: $countryFlagEmoji",
                modifier = Modifier.align(Alignment.CenterHorizontally).defaultItemPadding(),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }

        RespectShortVersionInfoText(Modifier.defaultItemPadding().fillMaxWidth())
    }
}
