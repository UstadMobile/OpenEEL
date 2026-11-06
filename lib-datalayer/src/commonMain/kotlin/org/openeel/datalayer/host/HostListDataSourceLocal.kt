package org.openeel.datalayer.host

import io.ktor.http.Url
import org.openeel.datalayer.respect.model.SchoolDirectory
import org.openeel.lib.dataloadstate.DataReadyState

interface HostListDataSourceLocal : HostListDataSource {

    suspend fun updateLocal(
        url: Url,
        dataLoadResult: DataReadyState<List<SchoolDirectory>>,
    )
}
