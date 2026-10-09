package world.respect.datalayer.schooldirectory

import io.ktor.http.Url
import io.ktor.util.StringValues
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.respect.model.RespectSchoolDirectory
import world.respect.lib.dataloadstate.DataLayerParams
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.respect.model.SchoolDirectoryEntry

interface SchoolDirectoryEntryDataSource {

    /**
     * @param name filter by school name
     * @param code an invite code with any directory prefix removed
     */
    data class GetListParams(
        val name: String? = null,
        val code: String? = null,
        val limit: Int = 200,
        val directoryUrl: Url? = null,
    ) {

        companion object {

            fun fromParams(stringValues: StringValues) : GetListParams {
                return GetListParams(
                    name = stringValues[PARAM_NAME],
                    code = stringValues[PARAM_CODE],
                    limit = stringValues[DataLayerParams.LIMIT]?.toIntOrNull() ?: DEFAULT_MAX_SCHOOL_LIST
                )
            }

        }

    }

    fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): Flow<DataLoadState<List<SchoolDirectoryEntry>>>

    suspend fun list(
        loadParams: DataLoadParams,
        listParams: GetListParams,
    ): DataLoadState<List<SchoolDirectoryEntry>>

    /**
     * Get the SchoolDirectoryEntry for a given url
     *
     * @param url The URL as per SchoolDirectoryEntry.self
     */
    suspend fun getSchoolDirectoryEntryByUrl(url: Url): DataLoadState<SchoolDirectoryEntry>

    companion object {

        const val PARAM_NAME = "name"

        const val PARAM_CODE = "code"

        const val DEFAULT_MAX_SCHOOL_LIST = 100

    }
}
