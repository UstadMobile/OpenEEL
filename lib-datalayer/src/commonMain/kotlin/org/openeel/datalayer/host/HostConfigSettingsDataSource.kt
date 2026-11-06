package org.openeel.datalayer.host

import io.ktor.http.Parameters
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.host.model.HostConfigSetting
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState

interface HostConfigSettingsDataSource {

    data class GetListParams(val key: String? = null) {

        fun toParameters(): Parameters = Parameters.build {
            key?.let { append("key", it) }
        }

        companion object {

            fun fromParams(params: StringValues): GetListParams = GetListParams(key = params["key"])

        }
    }

    fun listAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<HostConfigSetting>>>


    companion object {

        const val KEY_HOST_LIST = "host-list"

    }
}
