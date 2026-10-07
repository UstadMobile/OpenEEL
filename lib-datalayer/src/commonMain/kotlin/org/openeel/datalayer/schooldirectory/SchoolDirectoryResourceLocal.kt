package org.openeel.datalayer.schooldirectory

import org.openeel.datalayer.respect.model.SchoolDirectoryEntry

interface SchoolDirectoryResourceLocal: SchoolDirectoryResource {

    suspend fun setServerManagedSchoolConfig(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    )

}