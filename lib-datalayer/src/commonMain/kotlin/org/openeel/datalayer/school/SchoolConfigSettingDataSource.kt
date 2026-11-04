package org.openeel.datalayer.school

import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.school.model.SchoolConfigSetting
import org.openeel.datalayer.shared.WritableDataSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

interface SchoolConfigSettingDataSource: WritableDataSource<SchoolConfigSetting> {

    data class GetListParams(
        val common: GetListCommonParams = GetListCommonParams(),
        val key: String? = null,
    ) {

        companion object {

            fun fromParams(params: StringValues): GetListParams {
                return GetListParams(
                    common = GetListCommonParams.fromParams(params)
                )
            }
        }

    }

    suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting>

    fun listAsFlow(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): Flow<DataLoadState<List<SchoolConfigSetting>>>

    fun listAsPagingSource(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): IPagingSourceFactory<Int, SchoolConfigSetting>

    suspend fun list(
        loadParams: DataLoadParams = DataLoadParams(),
        params: GetListParams = GetListParams(),
    ): DataLoadState<List<SchoolConfigSetting>>

    override suspend fun store(list: List<SchoolConfigSetting>)

    companion object {

        const val ENDPOINT_NAME = "SchoolConfigSetting"

        const val KEY_APP_CATALOGS = "app-catalogs"

    }
}