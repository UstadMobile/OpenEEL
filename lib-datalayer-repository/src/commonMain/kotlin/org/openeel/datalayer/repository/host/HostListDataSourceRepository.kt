package org.openeel.datalayer.repository.host

import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.host.HostListDataSource
import org.openeel.datalayer.host.HostListDataSourceLocal
import org.openeel.datalayer.repository.ext.copyToValidateOnRemote
import org.openeel.datalayer.repository.flow.asRepoFlow
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.lib.dataloadstate.ext.takeIfShouldUpdateLocal

class HostListDataSourceRepository(
    val local: HostListDataSourceLocal,
    val remote: HostListDataSource,
) : HostListDataSource {

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: HostListDataSource.GetListParams,
    ): Flow<DataLoadState<List<SchoolDirectory>>> =
        local.listAsFlow(loadParams, params).asRepoFlow(
            dataLoadParams = loadParams,
            remoteFlow = { remote.listAsFlow(it, params) },
            onRemoteUpdated = { local.updateLocal(params.url, it) },
        )
}
