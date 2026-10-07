package org.openeel.datalayer

import org.openeel.datalayer.schooldirectory.SchoolDirectoryResource
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource

/**
 * School/organization directory datasource: used to find schools to connect to and store endpoint
 * info.
 */
interface SchoolDirectoryDataSource {

    val schoolDirectoryResource: SchoolDirectoryResource

    val schoolDirectoryEntryResource: SchoolDirectoryEntryResource

}
