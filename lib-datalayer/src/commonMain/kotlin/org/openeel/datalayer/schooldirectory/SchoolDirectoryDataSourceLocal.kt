package org.openeel.datalayer.schooldirectory

import org.openeel.datalayer.respect.model.SchoolDirectoryEntry

interface SchoolDirectoryDataSourceLocal: SchoolDirectoryDataSource {

    suspend fun setServerManagedSchoolConfig(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    )

}