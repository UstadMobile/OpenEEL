package world.respect.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.loading
import world.respect.shared.generated.resources.mappings
import world.respect.shared.generated.resources.policies_shared_devices
import world.respect.shared.generated.resources.school
import world.respect.shared.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToMapping: () -> Unit = {},
    onClickSchool: () -> Unit = {},
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
    ) {
        item {
            SettingsListItem(
                icon = Icons.Filled.Map,
                title = stringResource(Res.string.mappings),
                onClick = onNavigateToMapping,
                testTag = "mapping_setting_item"
            )
        }
        item {
            SettingsListItem(
                icon = Icons.Filled.School,
                title = stringResource(Res.string.school),
                onClick = onClickSchool,
                testTag = "shared_devices_item",
                description = stringResource(Res.string.policies_shared_devices),
            )
        }
    }
}

@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String,
    description: String? = null
) {
    ListItem(
        headlineContent = {
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = stringResource(Res.string.loading),
                tint = MaterialTheme.colorScheme.onSurface
            )
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

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
) {
    SettingsScreen()
    SettingsScreen(
        onClickSchool = viewModel::onClickSchool
    )
}
