package world.respect.datalayer.repository.schooldirectory

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal

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