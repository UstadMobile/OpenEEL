package world.respect.app.view.catalog.opdsfeededit

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
import world.respect.lib.opds.model.OpdsGroup
import world.respect.shared.generated.resources.Res
import world.respect.shared.generated.resources.add_item
import world.respect.shared.generated.resources.add_link_to_collection_here
import world.respect.shared.generated.resources.delete
import world.respect.shared.generated.resources.move
import world.respect.shared.generated.resources.section_title


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
            val navItems = group.navigation.orEmpty()

            ListItem(
                modifier = Modifier.clickable {
                    onClickAddPlaylist()
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(
                            start = (ICON_BUTTON_DEFAULT_WIDTH+8).dp,
                            end = 8.dp,
                        )
                    )
                },
                headlineContent = {
                    Text(text = stringResource(Res.string.add_link_to_collection_here))
                }
            )

            ReorderableColumn(
                list = navItems,
                onSettle = onGroupItemsReordered,
                modifier = Modifier.fillMaxWidth(),
            ) { itemIndex, navLink, _ ->
                //Would probably be nicer to use actual identifiers where possible.
                key(itemIndex) {
                    ReorderableItem {
                        OpdsFeedEditNavigationItemRow(
                            itemIndex = itemIndex,
                            navItem = navLink,
                            groupIndex = groupIndex,
                            hasMoveOption = showMoveItemOption,
                            onClickDelete = { onClickDeleteItem(itemIndex) },
                            onClickMove = { onClickMoveItem(itemIndex) },
                        )
                    }
                }
            }


        } else {
            val pubItems = group.publications ?: emptyList()

            ListItem(
                modifier = Modifier.clickable {
                    onClickAddItem()
                },
                leadingContent = {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.padding(
                            start = (ICON_BUTTON_DEFAULT_WIDTH+8).dp,
                            end = 8.dp,
                        )
                    )
                },
                headlineContent = {
                    Text(text = stringResource(Res.string.add_item))
                }
            )

            ReorderableColumn(
                list = pubItems,
                onSettle = onGroupItemsReordered,
                modifier = Modifier.fillMaxWidth(),
            ) { itemIndex, publication, _ ->
                key(itemIndex) {
                    ReorderableItem {
                        PlaylistPublicationItemRow(
                            itemIndex = itemIndex,
                            publication = publication,
                            groupIndex = groupIndex,
                            hasMovableSections = showMoveItemOption,
                            onClickDelete = { onClickDeleteItem(itemIndex) },
                            onClickMove = { onClickMoveItem(itemIndex) },
                        )
                    }
                }
            }
        }
    }
}
