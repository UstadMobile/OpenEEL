package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState


/**
 * This is based on Room RoomPagingUtil.kt to try and ensure that behavior is consistent with Room.
 */

const val INITIAL_ITEM_COUNT = -1

val INVALID = PagingSource.LoadResult.Invalid<Any, Any>()

/**
 * Calculates query limit based on LoadType.
 *
 * Prepend: If requested loadSize is larger than available number of items to prepend, it will
 * query with OFFSET = 0, LIMIT = prevKey
 */
fun getLimit(params: PagingSource.LoadParams<Int>, key: Int): Int {
    return when(params) {
        is PagingSource.LoadParams.Prepend<*> -> {
            if(key< params.loadSize) {
                key
            }else {
                params.loadSize
            }
        }
        else -> params.loadSize
    }
}

fun getOffset(params: PagingSource.LoadParams<Int>, key: Int, itemCount: Int): Int {
    return when(params) {
        is PagingSource.LoadParams.Prepend<*> -> {
            if(key < params.loadSize) {
                0
            }else {
                key - params.loadSize
            }
        }
        is PagingSource.LoadParams.Append<*> -> key
        is PagingSource.LoadParams.Refresh<*> -> {
            if (key >= itemCount) {
                maxOf(0, itemCount - params.loadSize)
            } else {
                key
            }
        }
    }
}


fun <Value : Any> PagingState<Int, Value>.getClippedRefreshKey(): Int? {
    return when (val anchorPosition = anchorPosition) {
        null -> null
        /**
         *  It is unknown whether anchorPosition represents the item at the top of the screen or item at
         *  the bottom of the screen. To ensure the number of items loaded is enough to fill up the
         *  screen, half of loadSize is loaded before the anchorPosition and the other half is
         *  loaded after the anchorPosition -- anchorPosition becomes the middle item.
         */
        else -> maxOf(0, anchorPosition - (config.initialLoadSize / 2))
    }
}


