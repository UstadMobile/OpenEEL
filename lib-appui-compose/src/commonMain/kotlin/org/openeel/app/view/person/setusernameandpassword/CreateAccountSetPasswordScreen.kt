package world.respect.app.view.person.setusernameandpassword

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPasswordField
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.password_label
import world.respect.shared.util.ext.isLoading
import world.respect.shared.viewmodel.person.setusernameandpassword.CreateAccountSetPasswordViewModel

@Composable
fun CreateAccountSetPasswordScreen(
    viewModel: CreateAccountSetPasswordViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val appUiState by viewModel.appUiState.collectAsState()

    RespectPasswordField(
        value = uiState.password,
        onValueChange = viewModel::onPasswordChanged,
        label = { Text(stringResource(Res.string.password_label)) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        isError = uiState.passwordErr != null,
        supportingText = uiState.passwordErr?.let {
            { Text(uiTextStringResource(it)) }
        },
        enabled = !appUiState.isLoading,
        modifier = Modifier
            .fillMaxWidth()
            .defaultItemPadding()
    )
}