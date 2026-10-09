package world.respect.app.view.person.copycode

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.defaultItemPadding
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.code
import world.respect.shared.generated.resources.code_private_info
import world.respect.shared.generated.resources.copy
import world.respect.shared.generated.resources.copycode
import world.respect.shared.generated.resources.empty_list
import world.respect.shared.viewmodel.person.copycode.CopyInviteCodeUiState
import world.respect.shared.viewmodel.person.copycode.CopyInviteCodeViewModel

@Composable
fun CopyInviteCodeScreen(viewModel: CopyInviteCodeViewModel) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    CopyInviteCodeScreen(
        uiState = uiState,
        onCopy = viewModel::copyCodeToClipboard,
    )
}

@Composable
fun CopyInviteCodeScreen(
    uiState: CopyInviteCodeUiState,
    onCopy: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))


        Image(
            painter = org.jetbrains.compose.resources.painterResource(Res.drawable.copycode),
            contentDescription = stringResource(resource = Res.string.empty_list),
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(200.dp)
        )


        Spacer(modifier = Modifier.height(12.dp))

        Text(text = stringResource(Res.string.code))

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            modifier = Modifier
                .defaultItemPadding()
                .fillMaxWidth()
                .testTag("invite_code_field"),
            value = uiState.code ?: "",
            onValueChange = {},
            readOnly = true,
            singleLine = true,
            label = { Text(stringResource(Res.string.code)) }
        )


        Button(
            onClick = onCopy,
            modifier = Modifier
                .fillMaxWidth()
                .defaultItemPadding()
                .testTag("invite_code"),
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = null)
            Text(
                text = stringResource(Res.string.copy),
            )
        }


        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            textAlign = TextAlign.Center,
            text = stringResource(Res.string.code_private_info),
        )


    }
}
