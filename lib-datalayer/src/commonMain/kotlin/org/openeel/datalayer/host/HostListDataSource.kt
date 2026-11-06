package org.openeel.datalayer.host

import io.ktor.http.Parameters
import io.ktor.util.StringValues
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState

interface HostListDataSource {

    data class GetListParams(val url: Url) {

        fun toParameters(): Parameters = Parameters.build {
            append("url", url.toString())
        }

        companion object {

            fun fromParams(params: StringValues): GetListParams =
                GetListParams(Url(requireNotNull(params["url"]) { "Host list URL is required" }))

        }
    }

    fun listAsFlow(
        loadParams: DataLoadParams,
        params: GetListParams,
    ): Flow<DataLoadState<List<SchoolDirectory>>>
}
