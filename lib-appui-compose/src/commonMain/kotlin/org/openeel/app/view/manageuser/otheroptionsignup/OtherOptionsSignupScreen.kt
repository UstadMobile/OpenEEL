package world.respect.app.view.manageuser.otheroptionsignup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectPasskeySignInFasterCard
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.sign_up_with_password
import world.respect.shared.viewmodel.manageuser.otheroptionsignup.OtherOptionsSignupUiState
import world.respect.shared.viewmodel.manageuser.otheroptionsignup.OtherOptionsSignupViewModel

@Composable
fun OtherOptionsSignupScreen(viewModel: OtherOptionsSignupViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    OtherOptionsSignupScreenContent(
        uiState = uiState,
        onClickPasswordSignup = viewModel::onClickSignupWithPassword,
        onClickPasskeySignup = viewModel::onClickSignupWithPasskey,
        onClickHowPasskeysWork = viewModel::onClickHowPasskeysWork,
    )
}

@Composable
fun OtherOptionsSignupScreenContent(
    uiState: OtherOptionsSignupUiState,
    onClickPasswordSignup: () -> Unit,
    onClickPasskeySignup: () -> Unit,
    onClickHowPasskeysWork: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        uiState.passkeyError?.let {
            Text(it)
        }

        OutlinedButton(
            onClick = onClickPasswordSignup,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(stringResource(Res.string.sign_up_with_password))
        }

        Spacer(modifier = Modifier.height(24.dp))

        RespectPasskeySignInFasterCard(
            onClickPasskeySignup = onClickPasskeySignup,
            onClickHowPasskeysWork = onClickHowPasskeysWork,
            generalError = uiState.generalError
        )
    }
}
