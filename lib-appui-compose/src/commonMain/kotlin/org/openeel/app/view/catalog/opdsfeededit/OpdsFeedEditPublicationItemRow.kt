package world.respect.app.view.catalog.opdsfeededit

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableListItemScope
import world.respect.app.components.langMapString
import world.respect.lib.opds.model.Publication
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.move


@Composable
fun ReorderableListItemScope.PlaylistPublicationItemRow(
    publication: Publication,
    itemIndex: Int,
    groupIndex: Int,
    hasMovableSections: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    ListItem(
        modifier = Modifier.fillMaxWidth().testTag("pub_item_${groupIndex}_$itemIndex"),
        leadingContent = {
            IconButton(
                onClick = { },
                modifier = Modifier
                    .padding(start = ICON_BUTTON_DEFAULT_WIDTH.dp)
                    .draggableHandle()
                    .testTag("pub_drag_handle_${groupIndex}_$itemIndex")
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = stringResource(Res.string.move),
                )
            }
        },
        headlineContent = { Text(text = langMapString(publication.metadata.title)) },
        supportingContent = {
            publication.metadata.description?.let { Text(text = it) }
        },
        trailingContent = {
            OpdsGroupItemMenuButton(
                groupIndex = groupIndex,
                itemIndex = itemIndex,
                hasMoveOption = hasMovableSections,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        },
    )
}