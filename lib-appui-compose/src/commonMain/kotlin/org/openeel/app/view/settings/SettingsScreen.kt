package org.openeel.app.view.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
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
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.loading
import org.openeel.shared.generated.resources.mappings
import org.openeel.shared.viewmodel.settings.SettingsViewModel

@Composable
fun SettingsScreen(
    onNavigateToMapping: () -> Unit = {},
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
    }
}

@Composable
private fun SettingsListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    ListItem(
        headlineContent = {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
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
}
