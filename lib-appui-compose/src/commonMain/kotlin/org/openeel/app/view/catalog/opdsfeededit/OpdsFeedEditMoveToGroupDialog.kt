package world.respect.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import world.respect.lib.opds.model.OpdsGroup
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.cancel
import world.respect.shared.generated.resources.move_to_section
import world.respect.shared.generated.resources.n_items
import world.respect.shared.generated.resources.section_title
import world.respect.shared.viewmodel.catalog.opdsfeededit.MovingItemState
import kotlin.collections.forEach


@Composable
fun OpdsFeedEditMoveToGroupDialog(
    compatibleSections: List<MovingItemState.CompatibleSection>,
    allSections: List<OpdsGroup>,
    onClickGroup: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(Res.string.move_to_section)) },
        text = {
            compatibleSections.forEach { section ->
                val actualSection = allSections[section.sectionIndex]
                val sectionTitle = actualSection.metadata.title
                    .takeIf { it.isNotBlank() }
                    ?: stringResource(Res.string.section_title)
                val itemCount = (actualSection.navigation?.size ?: 0) +
                        (actualSection.publications?.size ?: 0)
                ListItem(
                    headlineContent = { Text(text = sectionTitle) },
                    supportingContent = {
                        Text(text = stringResource(Res.string.n_items, itemCount))
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClickGroup(section.sectionIndex) },
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(Res.string.cancel))
            }
        },
    )
}