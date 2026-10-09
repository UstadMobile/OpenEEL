package world.respect.datalayer.repository

import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.datalayer.repository.schooldirectory.SchoolDirectoryEntryDataSourceRepository
import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class RespectAppDataSourceRepository(
    private val local: RespectAppDataSourceLocal,
    private val remote: RespectAppDataSource,
): RespectAppDataSource {

    /*
     * There is no remote school directory data source. SchoolDirectoryDataSource is simply a list of
     * the available directories.
     */
    override val schoolDirectoryDataSource: SchoolDirectoryDataSource by lazy {
        local.schoolDirectoryDataSource
    }

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource by lazy {
        SchoolDirectoryEntryDataSourceRepository(
            local.schoolDirectoryEntryDataSource, remote.schoolDirectoryEntryDataSource
        )
    }
}