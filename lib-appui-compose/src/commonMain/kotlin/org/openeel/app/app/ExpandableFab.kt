package world.respect.app.app

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import world.respect.app.components.uiTextStringResource
import world.respect.shared.viewmodel.app.appstate.ExpandableFabIcon
import world.respect.shared.viewmodel.app.appstate.ExpandableFabItem
import world.respect.shared.viewmodel.app.appstate.ExpandableFabUiState

@Composable
fun ExpandableFab(
    state: ExpandableFabUiState,
    onToggle: () -> Unit,
    onItemClick: (ExpandableFabItem) -> Unit
) {
    Column(
        Modifier.testTag("ExpandableFab"),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.End,
    ) {
        if (state.expanded) {
            state.items.forEach { item ->
                SmallFloatingActionButton(onClick = { onItemClick(item) }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp)
                            .testTag(uiTextStringResource(item.text))
                    ) {
                        Icon(
                            modifier = Modifier.padding(end = 8.dp),
                            imageVector = when (item.icon) {
                                ExpandableFabIcon.ADD -> Icons.Default.Add
                                ExpandableFabIcon.INVITE -> Icons.Default.Share
                                ExpandableFabIcon.LINK -> Icons.Default.Link
                            },
                            contentDescription = uiTextStringResource(item.text)
                        )
                        Text(text = uiTextStringResource(item.text))
                    }
                }
            }
        }

        val fabText = state.text
        val fabIcon = if (state.expanded) Icons.Default.Close else Icons.Default.Add

        if (fabText != null) {
            ExtendedFloatingActionButton(
                onClick = onToggle,
                icon = { Icon(imageVector = fabIcon, contentDescription = null) },
                text = { Text(text = uiTextStringResource(fabText)) },
            )
        } else {
            FloatingActionButton(onClick = onToggle) {
                Icon(imageVector = fabIcon, contentDescription = null)
            }
        }
    }
}
