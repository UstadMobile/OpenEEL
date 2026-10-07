package org.openeel.app.view.manageuser.enterpasswordsignup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.OpenEelPasswordField
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.password_label
import org.openeel.shared.generated.resources.required
import org.openeel.shared.generated.resources.sign_up
import org.openeel.shared.resources.StringResourceUiText
import org.openeel.shared.viewmodel.manageuser.enterpasswordsignup.EnterPasswordSignupUiState
import org.openeel.shared.viewmodel.manageuser.enterpasswordsignup.EnterPasswordSignupViewModel

@Composable
fun EnterPasswordSignupScreen(viewModel: EnterPasswordSignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    EnterPasswordSignupScreen(
        uiState = uiState,
        onPasswordChanged = viewModel::onPasswordChanged,
        onClickSignup = viewModel::onClickSignup
    )
}

@Composable
fun EnterPasswordSignupScreen(
    uiState: EnterPasswordSignupUiState,
    onPasswordChanged: (String) -> Unit,
    onClickSignup: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {

        OpenEelPasswordField(
            value = uiState.password,
            onValueChange = onPasswordChanged,
            label = { Text(stringResource(Res.string.password_label) + "*") },
            isError = uiState.passwordError != null,
            supportingText = {
                Text(
                    text = uiTextStringResource(
                        uiState.passwordError ?: StringResourceUiText(Res.string.required)
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        )


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickSignup,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text(stringResource(Res.string.sign_up))
        }
        uiState.generalError?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(uiTextStringResource(it))
        }
    }
}
