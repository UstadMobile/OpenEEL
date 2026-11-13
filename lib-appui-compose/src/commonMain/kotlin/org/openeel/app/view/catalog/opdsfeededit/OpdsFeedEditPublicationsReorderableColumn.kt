package org.openeel.app.view.catalog.opdsfeededit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.ktor.http.Url
import org.jetbrains.compose.resources.stringResource
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.util.OpdsFeedGroupItemItemIndex
import org.openeel.lib.opds.model.util.OpdsFeedItemIndex
import org.openeel.lib.opds.model.util.PublicationItemItemIndex
import org.openeel.shared.generated.resources.Res
import org.openeel.shared.generated.resources.add_item
import sh.calvin.reorderable.ReorderableColumn

@Composable
fun OpdsFeedEditPublicationsReorderableColumn(
    pubItems: List<Publication>,
    baseUrl: Url,
    groupIndex: Int,
    onClickAddItem: () -> Unit,
    onItemReorderSettle: (from: OpdsFeedItemIndex, toIndex: Int) -> Unit,
    showMoveItemOption: Boolean,
    onClickDeleteItem: (Int) -> Unit,
    onClickMoveItem: (Int) -> Unit,
    modifier: Modifier = Modifier,
    indentPadding: Dp = ICON_BUTTON_DEFAULT_WIDTH.dp,
) {
    Column(modifier = modifier) {
        ListItem(
            modifier = Modifier.clickable {
                onClickAddItem()
            },
            leadingContent = {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.padding(
                        start = indentPadding + 8.dp,
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
            onSettle = { from, toIndex ->
                onItemReorderSettle(
                    if(groupIndex >= 0) {
                        OpdsFeedGroupItemItemIndex(groupIndex, from)
                    } else{
                        PublicationItemItemIndex(from)
                    },
                    toIndex
                )
            },
            modifier = Modifier.fillMaxWidth(),
        ) { itemIndex, publication, _ ->
            key(itemIndex) {
                ReorderableItem {
                    OpdsFeedEditPublicationItemRow(
                        itemIndex = itemIndex,
                        publication = publication,
                        baseUrl = baseUrl,
                        groupIndex = groupIndex,
                        hasMovableSections = showMoveItemOption,
                        onClickDelete = { onClickDeleteItem(itemIndex) },
                        onClickMove = { onClickMoveItem(itemIndex) },
                        indentPadding = indentPadding,
                    )
                }
            }
        }
    }
}