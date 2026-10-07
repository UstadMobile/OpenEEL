package org.openeel.datalayer.http.school

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
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.lib.dataloadstate.DataLayerParams
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.ext.useTokenProvider
import org.openeel.datalayer.ext.useValidationCacheControl
import org.openeel.datalayer.http.ext.appendCommonListParams
import org.openeel.datalayer.http.ext.appendIfNotNull
import org.openeel.datalayer.http.ext.respectEndpointUrl
import org.openeel.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.EnrollmentDataSource
import org.openeel.datalayer.school.model.Enrollment
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.shared.DataLayerTags.TAG_DATALAYER
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

class EnrollmentDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
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