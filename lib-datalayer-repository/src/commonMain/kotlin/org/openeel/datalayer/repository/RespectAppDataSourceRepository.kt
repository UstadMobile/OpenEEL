package org.openeel.datalayer.repository

import org.openeel.datalayer.RespectAppDataSource
import org.openeel.datalayer.RespectAppDataSourceLocal
import org.openeel.datalayer.repository.schooldirectory.SchoolDirectoryEntryDataSourceRepository
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

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