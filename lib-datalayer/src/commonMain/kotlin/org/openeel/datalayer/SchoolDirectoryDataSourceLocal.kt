package org.openeel.datalayer

import org.openeel.datalayer.schooldirectory.SchoolDirectoryResourceLocal
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResourceLocal

/**
 *
 */
interface SchoolDirectoryDataSourceLocal: SchoolDirectoryDataSource {

    override val schoolDirectoryResource: SchoolDirectoryResourceLocal

    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResourceLocal

}
