package world.respect.app.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory

const val DEFAULT_PAGE_SIZE = 20

const val DEFAULT_MAX_SIZE = 200

/**
 * Simple shorthand for remember pager to include default values
 */
@Composable
fun <Key: Any, Value: Any> respectRememberPager(
    pagingSourceFactory: () -> PagingSource<Key, Value>,
): Pager<Key, Value> {
    return remember(pagingSourceFactory) {
        Pager(
            config = PagingConfig(DEFAULT_PAGE_SIZE, maxSize = DEFAULT_MAX_SIZE),
            pagingSourceFactory = pagingSourceFactory,
        )
    }
}

@Composable
fun <Key: Any, Value: Any> respectRememberPager(
    iPagingSourceFactory: IPagingSourceFactory<Key, Value>
): Pager<Key, Value> {
    val pagingSourceFactory = remember(iPagingSourceFactory) {
        { iPagingSourceFactory.invoke() }
    }
    return respectRememberPager(pagingSourceFactory)
}

