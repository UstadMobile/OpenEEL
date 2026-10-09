package world.respect.app.view.sharedschooldevice

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.app.components.uiTextStringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.devices
import world.respect.shared.generated.resources.school_name
import world.respect.shared.generated.resources.shared_school_devices
import world.respect.shared.viewmodel.sharedschooldevice.SchoolSettingsViewModel

@Composable
fun SchoolSettingsScreen(
    viewModel: SchoolSettingsViewModel,
) {
    val uiState by viewModel.uiState.collectAsState()
    Column {
        SchoolSettingsScreen(
            title = stringResource(Res.string.school_name),
            description =   uiState.schoolName?.let {
                uiTextStringResource(it)
            } ?: "",
            testTag = "my_school",
        )
        SchoolSettingsScreen(
            title = stringResource(Res.string.shared_school_devices),
            description = "${uiState.sharedSchoolDeviceCount.toString()} ${stringResource(Res.string.devices)}",
            testTag = "devices_count",
            onClick = viewModel::onClickSharedSchoolDevices
        )
    }

}

@Composable
fun SchoolSettingsScreen(
    title: String,
    description: String,
    onClick: () -> Unit = {},
    testTag: String
) {
    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        modifier = Modifier
            .testTag(testTag)
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        tonalElevation = 0.dp
    )
}