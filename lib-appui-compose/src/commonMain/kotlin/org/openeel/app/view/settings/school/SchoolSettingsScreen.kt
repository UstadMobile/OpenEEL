package org.openeel.app.view.settings.school

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import org.jetbrains.compose.resources.stringResource
import org.openeel.app.components.uiTextStringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.devices
import org.openeel.shared.generated.resources.school_name
import org.openeel.shared.generated.resources.shared_school_devices
import org.openeel.shared.viewmodel.settings.school.SchoolSettingsUiState
import org.openeel.shared.viewmodel.settings.school.SchoolSettingsViewModel

@Composable
fun SchoolSettingsScreen(
    viewModel: SchoolSettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    SchoolSettingsScreen(
        uiState = uiState,
        onClickSharedDevices = viewModel::onClickSharedSchoolDevices,
    )
}

@Composable
fun SchoolSettingsScreen(
    uiState: SchoolSettingsUiState,
    onClickSharedDevices: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        ListItem(
            modifier = Modifier.testTag("my_school").fillMaxWidth(),
            headlineContent = { Text(uiState.schoolName?.let { uiTextStringResource(it) } ?: "")},
            supportingContent = { Text(stringResource(Res.string.school_name)) },
        )

        ListItem(
            modifier = Modifier.clickable(onClick = onClickSharedDevices)
                .fillMaxWidth()
                .testTag("devices_count"),
            headlineContent = { Text(stringResource(Res.string.shared_school_devices)) },
            supportingContent = {
                Text("${uiState.sharedSchoolDeviceCount.toString()} ${stringResource(Res.string.devices)}")
            }
        )
    }
}
