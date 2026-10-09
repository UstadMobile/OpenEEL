package world.respect.datalayer

import world.respect.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal

/**
 *
 */
interface RespectAppDataSourceLocal: RespectAppDataSource {

    override val schoolDirectoryDataSource: SchoolDirectoryDataSourceLocal

    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal

}
