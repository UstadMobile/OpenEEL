package org.openeel.datalayer

import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal

/**
 *
 */
interface SchoolDirectoryDataSourceLocal: SchoolDirectoryDataSource {

    override val schoolDirectoryDataSource: SchoolDirectoryDataSourceLocal

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal

}
