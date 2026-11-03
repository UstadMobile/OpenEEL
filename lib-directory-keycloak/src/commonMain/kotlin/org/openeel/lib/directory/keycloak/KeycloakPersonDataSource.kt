package org.openeel.lib.directory.keycloak

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.adapters.asListDetails
import org.openeel.datalayer.school.model.composites.PersonListDetails
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.map
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.lib.dataloadstate.ext.mapDataReadyState
import org.openeel.lib.dataloadstate.throwable.HttpErrorResponseException
import org.openeel.lib.directory.model.Person
import org.openeel.lib.directory.resources.PersonDirectoryResource

class KeycloakPersonDataSource(
    private val directory: KeycloakPersonDirectoryResource,
) : PersonDataSource {

    override suspend fun findByUsername(username: String): Person? = when (
        val result = list(DataLoadParams(), PersonDataSource.GetListParams(filterByName = username))
    ) {
        is DataReadyState -> result.data.firstOrNull { it.username == username }
        is DataErrorResult -> throw result.error
        else -> null
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        return try {
            DataReadyState(directory.findByGuid(guid))
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: HttpErrorResponseException) {
            if (exception.statusCode == 404) {
                NoDataLoadedState.notFound()
            } else {
                DataErrorResult(exception)
            }
        } catch (exception: Exception) {
            DataErrorResult(exception)
        }
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> = flow {
        emit(findByGuid(DataLoadParams(), guid))
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): DataLoadState<List<Person>> {
        val guid = params.common.guid
        return if (guid != null) {
            findByGuid(loadParams, guid).mapDataReadyState {
                DataReadyState(listOf(it.data))
            }
        } else {
            directory.list(
                loadParams, PersonDirectoryResource.GetListParams(
                    filterByName = params.filterByName ?: params.common.searchQuery,
                    filterByPersonRole = params.filterByPersonRole,
                    filterByClazzUid = params.filterByClazzUid,
                    filterByEnrolmentRole = params.filterByEnrolmentRole,
                    includeRelated = params.includeRelated,
                )
            )
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> = flow { emit(list(loadParams, params)) }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, Person> = IPagingSourceFactory {
        object : PagingSource<Int, Person>() {
            override fun getRefreshKey(state: PagingState<Int, Person>): Int? = null

            override suspend fun load(pagingParams: LoadParams<Int>): LoadResult<Int, Person> {
                return when (val result = list(loadParams, params)) {
                    is DataReadyState -> LoadResult.Page(result.data, null, null)
                    is DataErrorResult -> LoadResult.Error(result.error)
                    else -> LoadResult.Page(emptyList(), null, null)
                }
            }
        }
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, PersonListDetails> = IPagingSourceFactory {
        listAsPagingSource(loadParams, listParams).invoke().map { it.asListDetails() }
    }

    override suspend fun store(list: List<Person>) {
        directory.store(list)
    }


}
