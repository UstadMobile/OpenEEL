package world.respect.datalayer.shared.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import io.github.aakira.napier.Napier

/**
 * Map a PagingSource where there is a 1:1 transform between T and R (e.g. from database entities to
 * model classes)
 */
internal class MapPagingSource<T: Any, R: Any>(
    private val src: PagingSource<Int, T>,
    private val transform: suspend (T) -> R,
    tag: LogPrefixFunction = NO_TAG,
): DelegatedInvalidationPagingSource<Int, R>(src, tag) {

    val logPrefix = "MapPagingSource(tag=${tag()})"

    override fun getRefreshKey(state: PagingState<Int, R>): Int? {
        return state.getClippedRefreshKey()
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, R> {
        Napier.d("$logPrefix: load ${params.toPrettyString()}")

        registerInvalidationCallbackIfNeeded()
        return when (val srcResult = src.load(params)) {
            is LoadResult.Page -> {
                LoadResult.Page(
                    data = srcResult.data.map {
                        transform(it)
                    },
                    prevKey = srcResult.prevKey,
                    nextKey = srcResult.nextKey,
                    itemsAfter = srcResult.itemsAfter,
                    itemsBefore = srcResult.itemsBefore
                )
            }

            is LoadResult.Error -> {
                Napier.e("$logPrefix: ERROR loading ${params.toPrettyString()}: error", throwable = srcResult.throwable)
                LoadResult.Error(srcResult.throwable)
            }

            is LoadResult.Invalid -> {
                Napier.e("$logPrefix: INVALID loading ${params.toPrettyString()}")
                LoadResult.Invalid()
            }
        }
    }
}

fun <T: Any, R: Any> PagingSource<Int, T>.map(
    tag: LogPrefixFunction = DelegatedInvalidationPagingSource.NO_TAG,
    transform: suspend (T) -> R
): PagingSource<Int, R> {
    return MapPagingSource(this, transform, tag)
}
