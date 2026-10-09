package world.respect.app.components

import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.runtime.Composable
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemKey


fun <T: Any> LazyGridScope.respectPagedItems(
    pagingItems: LazyPagingItems<T>,
    key: (T) -> Any,
    itemContent: @Composable (T?) -> Unit
) {
    items(
        count = pagingItems.itemCount,
        key = pagingItems.itemKey {
            key(it)
        }
    ) { index ->
        itemContent(pagingItems[index])
    }

}
