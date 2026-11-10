package org.openeel.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.policies_shared_devices
import org.openeel.shared.generated.resources.school
import org.openeel.shared.viewmodel.settings.SettingsUiState
import org.openeel.shared.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    uiState: SettingsUiState,
    onClickSchool: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        if(uiState.showSchoolSettings) {
            ListItem(
                modifier = Modifier.clickable {
                    onClickSchool()
                }.testTag("shared_devices_item")
                    .fillMaxWidth(),
                headlineContent = { Text(stringResource(Res.string.school)) },
                supportingContent = {
                    Text(stringResource(Res.string.policies_shared_devices))
                },
                leadingContent = {
                    Icon(Icons.Filled.Map, contentDescription = null)
                },
            )
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()

    SettingsScreen(
        uiState = uiState,
        onClickSchool = viewModel::onClickSchool,
    )
}
