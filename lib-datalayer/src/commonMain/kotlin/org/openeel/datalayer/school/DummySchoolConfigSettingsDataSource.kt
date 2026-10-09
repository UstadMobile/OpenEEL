package org.openeel.datalayer.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.school.model.SchoolConfigSetting
import org.openeel.datalayer.shared.paging.IPagingSourceFactory

class DummySchoolConfigSettingsDataSource(
    private val defaultAppCatalogUrl: String?,
): SchoolConfigSettingDataSource {

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolConfigSetting> {
        TODO("Not yet implemented")
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolConfigSetting> {
        TODO("Not yet implemented")
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolConfigSetting>>> {
        return flowOf(
            DataReadyState(
                data = defaultAppCatalogUrl?.let {
                    listOf(
                        SchoolConfigSetting(
                            key = SchoolConfigSettingDataSource.KEY_APP_CATALOGS,
                            value = it,
                        )
                    )
                } ?: emptyList()
            )
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolConfigSettingDataSource.GetListParams
    ): DataLoadState<List<SchoolConfigSetting>> {
        return DataReadyState(
            data = defaultAppCatalogUrl?.let {
                listOf(
                    SchoolConfigSetting(
                        key = SchoolConfigSettingDataSource.KEY_APP_CATALOGS,
                        value = it,
                    )
                )
            } ?: emptyList()
        )
    }

    override suspend fun store(list: List<SchoolConfigSetting>) {
        TODO("Not yet implemented")
    }
}