package world.respect.server.util.ext

import androidx.paging.PagingSource
import io.ktor.http.Parameters
import world.respect.lib.dataloadstate.DataLayerParams
import kotlin.text.toInt

fun Parameters.offsetLimitPagingLoadParams(): PagingSource.LoadParams<Int> {
    return PagingSource.LoadParams.Refresh(
        key = this[DataLayerParams.OFFSET]?.toInt() ?: 0,
        loadSize = this[DataLayerParams.LIMIT]?.toInt() ?: 1000,
        placeholdersEnabled = false
    )
}
