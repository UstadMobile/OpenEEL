package org.openeel.datalayer.repository

import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.repository.schooldirectory.SchoolDirectoryEntryDataSourceRepository
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class SchoolDirectoryDataSourceRepository(
    private val local: SchoolDirectoryDataSourceLocal,
    private val remote: SchoolDirectoryDataSource,
): SchoolDirectoryDataSource {

    /*
     * There is no remote school directory data source. SchoolDirectoryDataSource is simply a list of
     * the available directories.
     */
    override val schoolDirectoryDataSource: org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource by lazy {
        local.schoolDirectoryDataSource
    }

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource by lazy {
        SchoolDirectoryEntryDataSourceRepository(
            local.schoolDirectoryEntryDataSource, remote.schoolDirectoryEntryDataSource
        )
    }
}