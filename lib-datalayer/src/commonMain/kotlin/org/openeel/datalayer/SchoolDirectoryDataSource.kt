package org.openeel.datalayer

import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

/**
 * School/organization directory datasource: used to find schools to connect to and store endpoint
 * info.
 */
interface SchoolDirectoryDataSource {

    val schoolDirectoryDataSource: SchoolDirectoryDataSource

    val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource

}
