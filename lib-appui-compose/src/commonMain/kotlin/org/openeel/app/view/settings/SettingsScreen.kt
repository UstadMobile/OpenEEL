package org.openeel.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
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
import org.openeel.shared.generated.resources.mappings
import org.openeel.shared.generated.resources.policies_shared_devices
import org.openeel.shared.generated.resources.school
import org.openeel.shared.viewmodel.settings.SettingsViewModel
import org.openeel.shared.viewmodel.settings.SettingsUiState

@Composable
fun SettingsScreen(
    onNavigateToMapping: () -> Unit = {},
    onClickSchool: () -> Unit = {},
    showSchoolSettings: Boolean = false,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        item {
            ListItem(
                modifier = Modifier.clickable {
                    onNavigateToMapping()
                }.testTag("mapping_setting_item"),
                headlineContent = { Text(stringResource(Res.string.mappings)) },
                leadingContent = {
                    Icon(Icons.Filled.Map, contentDescription = null)
                }
            )
        }
        if (showSchoolSettings) {
            item {
                ListItem(
                    modifier = Modifier.clickable {
                        onClickSchool()
                    }.testTag("shared_devices_item"),
                    headlineContent = { Text(stringResource(Res.string.school)) },
                    supportingContent = {
                        Text(stringResource(Res.string.policies_shared_devices))
                    },
                    leadingContent = {
                        Icon(Icons.Filled.School, contentDescription = null)
                    }
                )
            }
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState(initial = SettingsUiState())
    SettingsScreen(
        onClickSchool = viewModel::onClickSchool,
        showSchoolSettings = uiState.showSchoolSettings,
    )
}
