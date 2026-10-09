package world.respect.app.view.schooldirectory.edit

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add
import world.respect.shared.generated.resources.example_url_placeholder
import world.respect.shared.generated.resources.link_label
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditUIState
import world.respect.shared.viewmodel.schooldirectory.edit.SchoolDirectoryEditViewModel


@Composable
fun SchoolDirectoryEditScreen(
    viewModel: SchoolDirectoryEditViewModel
) {
    val uiState by viewModel.uiState.collectAsState(Dispatchers.Main.immediate)
    SchoolDirectoryEditScreen(
        uiState = uiState,
        onLinkChanged = viewModel::onLinkChanged,
        onClickAdd = viewModel::onClickAdd,
    )
}

@Composable
fun SchoolDirectoryEditScreen(
    uiState: SchoolDirectoryEditUIState,
    onLinkChanged: (String) -> Unit,
    onClickAdd: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = uiState.linkUrl,
            onValueChange = onLinkChanged,
            label = {
                Text(
                    text = stringResource(Res.string.link_label)
                )
            },
            placeholder = {
                Text(
                    text = stringResource(Res.string.example_url_placeholder),
                )
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.errorMessage != null,
            supportingText = uiState.errorMessage?.let {
                {
                    Text(
                        text = uiTextStringResource(it)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClickAdd,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(Res.string.add),
            )
        }

    }
}