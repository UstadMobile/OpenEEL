package world.respect.datalayer.schooldirectory

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.respect.model.RespectSchoolDirectory

/**
 * DataSource to access all known directories
 */
interface SchoolDirectoryDataSource {

    suspend fun insertOrIgnore(
        schoolDirectory: RespectSchoolDirectory,
        clearOthers: Boolean = false,
    )

    suspend fun allDirectories(): List<RespectSchoolDirectory>

    fun allDirectoriesAsFlow(): Flow<List<RespectSchoolDirectory>>

    suspend fun deleteDirectory(directory: RespectSchoolDirectory)

}