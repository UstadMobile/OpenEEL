package world.respect.app.view.manageuser.getstarted

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.RespectExposedDropDownMenuField
import world.respect.app.components.RespectShortVersionInfoText
import world.respect.app.components.defaultItemPadding
import world.respect.app.components.langMapString
import world.respect.app.components.uiTextStringResource
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_my_school
import world.respect.shared.generated.resources.school_name
import world.respect.shared.generated.resources.other_options
import world.respect.shared.generated.resources.scan_qr_code_badge
import world.respect.shared.generated.resources.school_directory
import world.respect.shared.generated.resources.school_name_placeholder
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedUiState
import world.respect.shared.viewmodel.manageuser.getstarted.GetStartedViewModel

@Composable
fun GetStartedScreen(
    viewModel: GetStartedViewModel
) {
    val uiState by viewModel.uiState.collectAsState(context = Dispatchers.Main.immediate)

    GetStartedScreen(
        uiState = uiState,
        onSchoolNameChanged = viewModel::onSchoolNameChanged,
        onClickOtherOptions = viewModel::onClickOtherOptions,
        onClickScanQRBadge = viewModel::onClickScanQRBadge,
        onSchoolSelected = viewModel::onSchoolSelected,
        onAddMySchool = viewModel::onClickAddMySchool,
        onDirectorySelected = viewModel::onDirectorySelected,
    )
}

@Composable
fun GetStartedScreen(
    uiState: GetStartedUiState,
    onSchoolNameChanged: (String) -> Unit,
    onSchoolSelected: (SchoolDirectoryEntry) -> Unit,
    onClickOtherOptions: () -> Unit,
    onClickScanQRBadge: () -> Unit,
    onAddMySchool: () -> Unit,
    onDirectorySelected: (RespectSchoolDirectory) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .defaultItemPadding()
    ) {
        uiState.warning?.also {
            Row(Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                )

                Spacer(Modifier.width(16.dp))

                Text(
                    text = uiTextStringResource(it),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        uiState.selectedDirectory?.also { selectedDirectory ->
            RespectExposedDropDownMenuField(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                value = selectedDirectory,
                options = uiState.directoryOptions,
                onOptionSelected = onDirectorySelected,
                label = {
                    Text(text = stringResource(Res.string.school_directory))
                },
                itemText = { item ->
                    buildString {
                        item.name?.also {
                            append(it)
                            append(" - ")
                        }
                        append(item.baseUrl.toString())
                    }
                }
            )
        }

        OutlinedTextField(
            value = uiState.schoolName,
            onValueChange = onSchoolNameChanged,
            label = {
                Text(text = stringResource(Res.string.school_name))
            },
            placeholder = {
                Text(text = stringResource(Res.string.school_name_placeholder))
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
            modifier = Modifier.testTag("school_name")
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .testTag("school_name"),
            isError = uiState.errorMessage != null,
            supportingText = uiState.errorMessage?.let {
                { Text(uiTextStringResource(it)) }
            }
        )

        uiState.errorText?.let {
            Text(uiTextStringResource(it))
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth().testTag("schools_list")
        ) {
            items(
                count = uiState.suggestions.size,
                key = { index -> uiState.suggestions[index].self.toString() }
            ) { index ->
                val school = uiState.suggestions[index]
                ListItem(
                    modifier = Modifier
                        .testTag("school_list_item")
                        .fillMaxWidth()
                        .clickable { onSchoolSelected(school) },
                    headlineContent = {
                        Text(
                            modifier = Modifier.testTag("school_name_text"),
                            text = langMapString(school.name),
                        )
                    },
                    supportingContent = {
                        Text(
                            text = school.self.toString(),
                            maxLines = 1
                        )
                    },
                )
            }
        }
        if (uiState.showAddMySchool) {
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onAddMySchool() }
                    .testTag("add_my_school"),
                horizontalArrangement = Arrangement.Start
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(Res.string.add_my_school),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(Res.string.add_my_school),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        if (uiState.showButtons){
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = onClickScanQRBadge,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.scan_qr_code_badge))
            }
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedButton(
                onClick = onClickOtherOptions,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = stringResource(Res.string.other_options))
            }
        }

        RespectShortVersionInfoText(Modifier.defaultItemPadding().fillMaxWidth())
    }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}
