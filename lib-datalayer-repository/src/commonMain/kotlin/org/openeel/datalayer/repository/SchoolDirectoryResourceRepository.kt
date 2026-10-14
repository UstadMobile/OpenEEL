package org.openeel.datalayer.repository

import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.repository.schooldirectory.SchoolDirectoryEntryResourceRepository
import org.openeel.datalayer.schooldirectory.SchoolDirectoryResource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource

class SchoolDirectoryResourceRepository(
    private val local: SchoolDirectoryDataSourceLocal,
    private val remote: SchoolDirectoryDataSource,
): SchoolDirectoryDataSource {

    /*
     * There is no remote school directory data source. SchoolDirectoryDataSource is simply a list of
     * the available directories.
     */
    override val schoolDirectoryResource: SchoolDirectoryResource by lazy {
        local.schoolDirectoryResource
    }

    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource by lazy {
        SchoolDirectoryEntryResourceRepository(
            local.schoolDirectoryEntryResource, remote.schoolDirectoryEntryResource
        )
    }
}