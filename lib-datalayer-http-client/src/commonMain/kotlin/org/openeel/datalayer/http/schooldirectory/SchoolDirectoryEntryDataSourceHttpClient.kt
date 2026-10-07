package org.openeel.datalayer.http.schooldirectory

import io.ktor.client.HttpClient
import io.ktor.client.plugins.retry
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataLoadingState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.SchoolDirectoryDataSourceLocal
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.respect.model.RESPECT_SCHOOL_JSON_PATH
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import org.openeel.lib.dataloadstate.ext.map
import org.openeel.libutil.ext.appendEndpointSegments
import org.openeel.libutil.ext.resolve
import kotlin.collections.map

class SchoolDirectoryEntryDataSourceHttpClient(
    private val httpClient: HttpClient,
    private val local : SchoolDirectoryDataSourceLocal,
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