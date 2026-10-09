package world.respect.datalayer.schooldirectory

import world.respect.datalayer.respect.model.SchoolDirectoryEntry

interface SchoolDirectoryDataSourceLocal: SchoolDirectoryDataSource {

    suspend fun setServerManagedSchoolConfig(
        school: SchoolDirectoryEntry,
        dbUrl: String,
    )

}