package org.openeel.datalayer

import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal

/**
 *
 */
interface RespectAppDataSourceLocal: RespectAppDataSource {

    override val schoolDirectoryDataSource: SchoolDirectoryDataSourceLocal

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal

}
