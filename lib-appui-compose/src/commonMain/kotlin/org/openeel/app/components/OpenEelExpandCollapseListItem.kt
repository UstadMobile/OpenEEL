package org.openeel.app.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.collapse
import org.openeel.shared.generated.resources.expand

/**
 * A standard list item that can be clicked to expand or collapse a section.
 * Displays a rotating arrow icon as trailing content indicating [expanded] state.
 */
@Composable
fun OpenEelExpandCollapseListItem(
    expanded: Boolean,
    onExpandedChanged: (Boolean) -> Unit,
    headlineContent: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    leadingContent: (@Composable () -> Unit)? = null,
    supportingContent: (@Composable () -> Unit)? = null,
    overlineContent: (@Composable () -> Unit)? = null,
    colors: ListItemColors = ListItemDefaults.colors(),
    tonalElevation: Dp = ListItemDefaults.Elevation,
    shadowElevation: Dp = ListItemDefaults.Elevation,
    trailingContent: (@Composable () -> Unit)? = {
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowUp,
            contentDescription = stringResource(if(expanded) Res.string.collapse else Res.string.expand),
            modifier = Modifier
                .size(24.dp)
                .rotate(if (expanded) 0f else -180f),
        )
    },
) {
    ListItem(
        headlineContent = headlineContent,
        modifier = modifier.clickable(onClick = { onExpandedChanged(!expanded) }),
        overlineContent = overlineContent,
        supportingContent = supportingContent,
        leadingContent = leadingContent,
        trailingContent = trailingContent,
        colors = colors,
        tonalElevation = tonalElevation,
        shadowElevation = shadowElevation,
    )
}
