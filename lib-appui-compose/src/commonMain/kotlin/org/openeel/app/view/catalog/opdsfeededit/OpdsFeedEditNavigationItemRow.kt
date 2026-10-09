package world.respect.app.view.catalog.opdsfeededit

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
import world.respect.lib.opds.model.ReadiumLink
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.move


@Composable
fun ReorderableListItemScope.OpdsFeedEditNavigationItemRow(
    navItem: ReadiumLink,
    itemIndex: Int,
    groupIndex: Int,
    hasMoveOption: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
) {
    ListItem(
        modifier = Modifier.testTag("nav_item_${groupIndex}_$itemIndex")
            ,
        leadingContent = {
            IconButton(
                onClick = {},
                modifier = Modifier
                    .padding(start = ICON_BUTTON_DEFAULT_WIDTH.dp)
                    .draggableHandle()
                    .testTag("nav_drag_handle_${groupIndex}_$itemIndex")
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = stringResource(Res.string.move),
                )
            }
        },
        headlineContent = {
            Text(
                text = navItem.title?.takeIf { it.isNotBlank() } ?: navItem.href,
            )
        },
        trailingContent = {
            OpdsGroupItemMenuButton(
                groupIndex = groupIndex,
                itemIndex = itemIndex,
                hasMoveOption = hasMoveOption,
                onClickDelete = onClickDelete,
                onClickMove = onClickMove,
            )
        },
    )
}