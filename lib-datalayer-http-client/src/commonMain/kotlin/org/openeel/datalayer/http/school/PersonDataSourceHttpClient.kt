package org.openeel.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import io.ktor.util.reflect.typeInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.lib.dataloadstate.DataLayerParams
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.ext.useTokenProvider
import org.openeel.datalayer.ext.useValidationCacheControl
import org.openeel.datalayer.http.ext.appendIfNotNull
import org.openeel.datalayer.http.ext.appendCommonListParams
import org.openeel.datalayer.http.ext.respectEndpointUrl
import org.openeel.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.adapters.asListDetails
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.composites.PersonListDetails
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.map
import org.openeel.datalayer.shared.params.GetListCommonParams

class PersonDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonDataSource, SchoolUrlBasedDataSource {

    private suspend fun PersonDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(PersonDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_CLASS_UID, filterByClazzUid)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_ENROLLMENT_ROLE, filterByEnrolmentRole?.value)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_PERSON_STATUS, filterByPersonStatus?.value)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_NAME, filterByName)
                parameters.appendIfNotNull(DataLayerParams.INCLUDE_RELATED, includeRelated.toString())
                parameters.appendIfNotNull(DataLayerParams.IN_CLASS_ON_DAY, inClassOnDay?.toString())
            }
            .build()
    }

    override suspend fun findByUsername(username: String): Person {
        TODO("Not yet implemented")
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        return httpClient.getAsDataLoadState<List<Person>>(
            PersonDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        return httpClient.getDataLoadResultAsFlow<List<Person>>(
            urlFn = {
                PersonDataSource.GetListParams(
                    GetListCommonParams(guid = guid)
                ).urlWithParams()
            },
            dataLoadParams = DataLoadParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.map {
            it.firstOrNotLoaded()
        }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> {
        return httpClient.getDataLoadResultAsFlow<List<Person>>(
            urlFn = { params.urlWithParams() },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): DataLoadState<List<Person>> {
        return httpClient.getAsDataLoadState<List<Person>>(
            url = params.urlWithParams(),
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, Person> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Person>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                logPrefixExtra =  { "Person-HTTP-listAsPagingSource(params=$params)" },
            )
        }
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams
    ): IPagingSourceFactory<Int, PersonListDetails> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource<Person>(
                baseUrlProvider = { listParams.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Person>>(),
            ).map(tag = { "PersonHttp-listDetails(params=$listParams)" }) { person ->
                person.asListDetails()
            }
        }
    }

    override suspend fun store(list: List<Person>) {
        httpClient.post(
            url = respectEndpointUrl(PersonDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}