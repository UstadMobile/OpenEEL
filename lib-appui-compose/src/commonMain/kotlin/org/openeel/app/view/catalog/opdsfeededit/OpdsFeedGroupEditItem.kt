package org.openeel.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableColumn
import sh.calvin.reorderable.ReorderableListItemScope
import org.openeel.lib.opds.model.OpdsGroup
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_item
import org.openeel.shared.generated.resources.add_link_to_collection_here
import org.openeel.shared.generated.resources.delete
import org.openeel.shared.generated.resources.move
import org.openeel.shared.generated.resources.section_title


@Composable
fun ReorderableListItemScope.OpdsFeedGroupEditItem(
    group: OpdsGroup,
    groupIndex: Int,
    showMoveItemOption: Boolean,
    onGroupTitleChanged: (String) -> Unit,
    onClickDeleteGroup: () -> Unit,
    onClickAddItem: () -> Unit,
    onClickAddPlaylist: () -> Unit,
    onClickDeleteItem: (Int) -> Unit,
    onClickMoveItem: (Int) -> Unit,
    onGroupItemsReordered: (fromIndex: Int, toIndex: Int) -> Unit,
) {
    val isNavigationSection = group.navigation != null

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        ListItem(
            leadingContent = {
                IconButton(
                    onClick = { },
                    modifier = Modifier.draggableHandle()
                        .testTag("section_drag_handle_$groupIndex"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = stringResource(Res.string.move),
                    )
                }
            },
            headlineContent = {
                OutlinedTextField(
                    value = group.metadata.title,
                    onValueChange = onGroupTitleChanged,
                    label = { Text(stringResource(Res.string.section_title)) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("section_title_field_$groupIndex"),
                    singleLine = true,
                )
            },
            trailingContent = {
                IconButton(
                    onClick = onClickDeleteGroup,
                    modifier = Modifier.testTag("delete_section_$groupIndex"),
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = stringResource(Res.string.delete),
                    )
                }
            }
        )

        if (isNavigationSection) {
            NavigationItemsReorderableColumn(
                navItems = group.navigation.orEmpty(),
                groupIndex = groupIndex,
                showMoveItemOption = showMoveItemOption,
                onClickAdd = onClickAddPlaylist,
                onItemsReordered = onGroupItemsReordered,
                onClickDeleteItem = onClickDeleteItem,
                onClickMoveItem = onClickMoveItem,
                modifier = Modifier.fillMaxWidth(),
            )
        } else {
            OpdsFeedEditPublicationsReorderableColumn(
                pubItems = group.publications.orEmpty(),
                groupIndex = groupIndex,
                onClickAddItem = onClickAddItem,
                onItemsReordered = onGroupItemsReordered,
                showMoveItemOption = showMoveItemOption,
                onClickDeleteItem = onClickDeleteItem,
                onClickMoveItem = onClickMoveItem,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
