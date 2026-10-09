package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource

fun PagingSource.LoadParams<*>.toPrettyString() : String {
    val typeName = when(this) {
        is PagingSource.LoadParams.Refresh -> "Refresh"
        is PagingSource.LoadParams.Append -> "Append"
        is PagingSource.LoadParams.Prepend -> "Prepend"
    }

    return "$typeName(key=$key, loadSize=$loadSize)"
}

fun <T: Any> PagingSource.LoadParams<T>.copyWithLoadSize(
    transform: (Int) -> Int,
): PagingSource.LoadParams<T> {
    val newLoadSize = transform(loadSize)

    return when(this) {
        is PagingSource.LoadParams.Refresh -> {
            PagingSource.LoadParams.Refresh(
                key = key, loadSize = newLoadSize, placeholdersEnabled = placeholdersEnabled
            )
        }
        is PagingSource.LoadParams.Append -> {
            PagingSource.LoadParams.Append(
                key = key, loadSize = newLoadSize, placeholdersEnabled = placeholdersEnabled
            )
        }
        is PagingSource.LoadParams.Prepend -> {
            PagingSource.LoadParams.Prepend(
                key = key, loadSize = newLoadSize, placeholdersEnabled = placeholdersEnabled
            )
        }
    }
}
