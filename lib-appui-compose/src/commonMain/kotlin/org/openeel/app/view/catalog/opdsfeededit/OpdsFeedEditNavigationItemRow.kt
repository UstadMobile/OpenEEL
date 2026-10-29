package org.openeel.app.view.catalog.opdsfeededit

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import sh.calvin.reorderable.ReorderableListItemScope
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.libutil.ext.resolve
import org.openeel.shared.ext.findIcons
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.move


@Composable
fun ReorderableListItemScope.OpdsFeedEditNavigationItemRow(
    navItem: ReadiumLink,
    baseUrl: Url,
    itemIndex: Int,
    groupIndex: Int,
    hasMoveOption: Boolean,
    onClickDelete: () -> Unit,
    onClickMove: () -> Unit,
    indentPadding: Dp = ICON_BUTTON_DEFAULT_WIDTH.dp
) {
    ListItem(
        modifier = Modifier.testTag("nav_item_${groupIndex}_$itemIndex")
            ,
        leadingContent = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {},
                    modifier = Modifier
                        .padding(start = indentPadding)
                        .draggableHandle()
                        .testTag("nav_drag_handle_${groupIndex}_$itemIndex")
                ) {
                    Icon(
                        imageVector = Icons.Filled.DragHandle,
                        contentDescription = stringResource(Res.string.move),
                    )
                }

                Spacer(Modifier.size(8.dp))

                navItem.findIcons().firstOrNull()?.also {
                    AsyncImage(
                        model = baseUrl.resolve(it.href).toString(),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
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