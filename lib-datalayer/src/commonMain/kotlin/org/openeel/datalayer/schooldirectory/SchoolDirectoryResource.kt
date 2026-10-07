package org.openeel.datalayer.schooldirectory

import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.respect.model.SchoolDirectory

/**
 * DataSource to access all known directories
 */
interface SchoolDirectoryResource {

    suspend fun insertOrIgnore(
        schoolDirectory: SchoolDirectory,
        clearOthers: Boolean = false,
    )

    suspend fun allDirectories(): List<SchoolDirectory>

    fun allDirectoriesAsFlow(): Flow<List<SchoolDirectory>>

    suspend fun deleteDirectory(directory: SchoolDirectory)

}