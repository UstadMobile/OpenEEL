package world.respect.app.components

import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems

/**
 * Function to avoid common pitfalls with using LazyPagingItems, specifically avoids calling
 * LazyPagingItems.get in the key or contentType function (which leads to infinite loops)
 */
fun <T: Any> LazyListScope.respectPagingItems(
    items: LazyPagingItems<T>,
    key: ((item: T?, index: Int) -> Any)?,
    contentType: (item: T?) -> Any? = { null },
    itemContent: @Composable LazyItemScope.(item: T?) -> Unit,
) {
    items(
        count = items.itemCount,
        key = if(key != null) {
            { index -> key(items.peek(index), index) }
        }else {
            null
        },
        contentType = { index -> contentType(items.peek(index)) }
    ) { index ->
        itemContent(items[index])
    }

}