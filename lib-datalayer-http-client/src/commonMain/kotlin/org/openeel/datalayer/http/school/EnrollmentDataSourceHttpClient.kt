package world.respect.datalayer.http.school

import io.github.aakira.napier.Napier
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
import world.respect.datalayer.AuthTokenProvider
import world.respect.lib.dataloadstate.DataLayerParams
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.firstOrNotLoaded
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.appendIfNotNull
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.datalayer.shared.DataLayerTags.TAG_DATALAYER
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.params.GetListCommonParams

class EnrollmentDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
): EnrollmentDataSource, SchoolUrlBasedDataSource {

    private suspend fun EnrollmentDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(EnrollmentDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_CLASS_UID, classUid)
                parameters.appendIfNotNull(DataLayerParams.FILTER_BY_ENROLLMENT_ROLE,
                    role?.value)
                parameters.appendIfNotNull(EnrollmentDataSource.FILTER_BY_PERSON_UID,
                    personUid)
                parameters.appendIfNotNull(DataLayerParams.ACTIVE_ON_DAY,
                    activeOnDay?.toString())
                parameters.append(DataLayerParams.ORDER_BY, orderBy.orderOption.name)
            }.build()
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Enrollment> {
        return httpClient.getAsDataLoadState<List<Enrollment>>(
            EnrollmentDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<Enrollment>> {
        return httpClient.getDataLoadResultAsFlow<List<Enrollment>>(
            urlFn = {
                EnrollmentDataSource.GetListParams(
                    GetListCommonParams(guid = guid)
                ).urlWithParams()
            },
            dataLoadParams = loadParams,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.map {
            it.firstOrNotLoaded()
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Enrollment> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { listParams.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Enrollment>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                },
                logPrefixExtra = { "EnrollmentDataSource params=$listParams"}
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): DataLoadState<List<Enrollment>> {
        return httpClient.getAsDataLoadState<List<Enrollment>>(
            url = listParams.urlWithParams(),
            validationHelper = validationHelper
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<Enrollment>) {
        val url = respectEndpointUrl(EnrollmentDataSource.ENDPOINT_NAME)
        val response = httpClient.post(url) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }

        Napier.d(tag = TAG_DATALAYER) {
            "EnrollmentDataSourceHttp: posted ${list.size} items(${list.joinToString { it.uid }}) to $url (status=${response.status.value}"
        }
    }
}