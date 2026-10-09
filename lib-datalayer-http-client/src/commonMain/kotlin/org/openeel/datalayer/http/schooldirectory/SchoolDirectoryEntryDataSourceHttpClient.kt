package world.respect.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.client.plugins.retry
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataLoadingState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.RespectAppDataSourceLocal
import world.respect.lib.dataloadstate.ext.dataOrNull
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.ext.map
import world.respect.libutil.ext.appendEndpointSegments
import world.respect.libutil.ext.resolve
import kotlin.collections.map

class SchoolDirectoryEntryDataSourceHttpClient(
    private val httpClient: HttpClient,
    private val local : RespectAppDataSourceLocal,
): SchoolDirectoryEntryDataSource {

    /**
     * List all available SchoolDirectoryEntry(s). This will (concurrently) send requests to all
     * known school directories (avoids delay fetching from working servers if one or more other
     * servers are unreachable).
     */
    override fun listAsFlow(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): Flow<DataLoadState<List<SchoolDirectoryEntry>>> {
        return flow {
            val directories = local.schoolDirectoryDataSource.allDirectories()
            val flows = directories.filter {
                listParams.directoryUrl == null || it.baseUrl == listParams.directoryUrl
            }.map { dir ->
                httpClient.getDataLoadResultAsFlow<List<SchoolDirectoryEntry>>(
                    url = dir.baseUrl.appendEndpointSegments("api/directory/school"),
                    dataLoadParams = loadParams,
                ) {
                    retry {
                        retryOnExceptionOrServerErrors(DEFAULT_MAX_RETRIES)
                    }

                    headers[HttpHeaders.CacheControl] = "no-store"

                }.map { dataLoadState ->
                    dataLoadState.map { list ->
                        list.map { it.copy(inDirectoryUrl = dir.baseUrl) }
                    }
                }
            }

            emitAll(
                combine(flows = flows) { dataLoadStates ->
                    val data = buildList {
                        dataLoadStates.forEach {
                            it.dataOrNull()?.also(::addAll)
                        }
                    }

                    when {
                        dataLoadStates.all { it is DataReadyState } -> {
                            DataReadyState(data = data)
                        }

                        dataLoadStates.any { it is DataLoadingState } -> {
                            DataLoadingState(partialData = data)
                        }

                        data.isEmpty() && dataLoadStates.any { it is DataErrorResult } -> {
                            DataErrorResult(
                                error = dataLoadStates.firstNotNullOfOrNull {
                                    (it as? DataErrorResult)?.error
                                } ?: IllegalStateException()
                            )
                        }

                        else -> {
                            DataReadyState(data = data)
                        }
                    }
                }
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: SchoolDirectoryEntryDataSource.GetListParams
    ): DataLoadState<List<SchoolDirectoryEntry>> {
        val directories = local.schoolDirectoryDataSource.allDirectories()
        val listEntries = directories.filter {
            listParams.directoryUrl == null || it.baseUrl == listParams.directoryUrl
        }.map { dir ->
            httpClient.getAsDataLoadState<List<SchoolDirectoryEntry>>(
                dir.baseUrl.appendEndpointSegments("api/directory/school")
            ) {
                headers[HttpHeaders.CacheControl] = "no-store"

                retry {
                    retryOnExceptionOrServerErrors(DEFAULT_MAX_RETRIES)
                }
            }.map { list ->
                list.map { it.copy(inDirectoryUrl = dir.baseUrl) }
            }
        }

        return DataReadyState(
            data = listEntries.flatMap {
                it.dataOrNull() ?: emptyList()
            }
        )
    }

    override suspend fun getSchoolDirectoryEntryByUrl(
        url: Url
    ): DataLoadState<SchoolDirectoryEntry> {
        return httpClient.getAsDataLoadState<SchoolDirectoryEntry>(
            url.resolve(RESPECT_SCHOOL_JSON_PATH)
        )
    }

    companion object {

        const val DEFAULT_MAX_RETRIES = 3

    }
}