package org.openeel.app.view.manageuser.otheroption

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.defaultItemPadding
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.enter_school_link
import org.openeel.shared.generated.resources.manage_school_directories
import org.openeel.shared.generated.resources.next
import org.openeel.shared.generated.resources.paste_link_here
import org.openeel.shared.viewmodel.manageuser.otheroption.OtherOptionsUiState
import org.openeel.shared.viewmodel.manageuser.otheroption.OtherOptionsViewModel

@Composable
fun OtherOptionsScreen(
    viewModel: OtherOptionsViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    OtherOptionsScreen(
        uiState = uiState,
        onLinkChanged = viewModel::onLinkChanged,
        onClickNext = viewModel::onClickNext,
        onClickManageSchoolDirectories = viewModel::onClickManageSchoolDirectories,
    )
}

@Composable
fun OtherOptionsScreen(
    uiState: OtherOptionsUiState,
    onLinkChanged: (String) -> Unit,
    onClickNext: () -> Unit,
    onClickManageSchoolDirectories: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        Text(stringResource(Res.string.enter_school_link))
        OutlinedTextField(
            value = uiState.link,
            onValueChange = onLinkChanged,
            label = { Text(stringResource(Res.string.paste_link_here)) },
            placeholder = { Text(stringResource(Res.string.paste_link_here)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
            modifier = Modifier.fillMaxWidth().testTag("school_url"),
            isError = uiState.errorMessage != null,
            supportingText = uiState.errorMessage?.let {
                { Text(uiTextStringResource(it)) }
            }
        )
        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = onClickNext,
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        ) {
            Text(
                text = stringResource(Res.string.next),
            )
        }

        if(uiState.manageDirectoriesVisible) {
            OutlinedButton(
                onClick = onClickManageSchoolDirectories,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            ) {
                Text(stringResource(Res.string.manage_school_directories))
            }
        }
    }
}
