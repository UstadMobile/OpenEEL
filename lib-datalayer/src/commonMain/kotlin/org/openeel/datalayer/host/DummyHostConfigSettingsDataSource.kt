package org.openeel.datalayer.host

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.openeel.datalayer.host.model.HostConfigSetting
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState

class DummyHostConfigSettingsDataSource(
    defaultHostListUrl: String,
) : HostConfigSettingsDataSource {

    private val settings = listOf(
        HostConfigSetting(
            key = HostConfigSettingsDataSource.KEY_HOST_LIST,
            value = defaultHostListUrl,
        )
    )

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: HostConfigSettingsDataSource.GetListParams,
    ): Flow<DataLoadState<List<HostConfigSetting>>> =
        flowOf(DataReadyState(settings.filter { params.key == null || it.key == params.key }))

}
