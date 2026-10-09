package org.openeel.datalayer.repository.schooldirectory

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.ext.updateFromRemoteListIfNeeded
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal

class SchoolDirectoryEntryDataSourceRepository(
    private val local: SchoolDirectoryEntryDataSourceLocal,
    private val remote: SchoolDirectoryEntryDataSource,
) : SchoolDirectoryEntryDataSource {

    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return local.listAsFlow(loadParams, listParams).combineWithRemote(
            remote.listAsFlow(loadParams, listParams).onEach {
                local.updateFromRemoteListIfNeeded(it, null)
            }
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): DataLoadState<List<SchoolDirectoryEntry>> {
        val remote = remote.list(loadParams, listParams)

        local.updateFromRemoteListIfNeeded(remote, null)
        return local.list(loadParams, listParams).combineWithRemote(remote)
    }

    override suspend fun getSchoolDirectoryEntryByUrl(
        url: Url
    ): DataLoadState<SchoolDirectoryEntry> {
        return local.getSchoolDirectoryEntryByUrl(url).takeIf { it is DataReadyState }
            ?: remote.getSchoolDirectoryEntryByUrl(url).also {
                if(it is DataReadyState) {
                    local.updateLocal(listOf(it.data))
                }
            }
    }
}