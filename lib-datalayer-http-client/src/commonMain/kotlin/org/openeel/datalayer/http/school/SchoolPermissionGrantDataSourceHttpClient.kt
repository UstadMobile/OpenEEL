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
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.firstOrNotLoaded
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.ext.useTokenProvider
import org.openeel.datalayer.ext.useValidationCacheControl
import org.openeel.datalayer.http.ext.appendCommonListParams
import org.openeel.datalayer.http.ext.respectEndpointUrl
import org.openeel.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.SchoolPermissionGrantDataSource
import org.openeel.datalayer.school.model.SchoolPermissionGrant
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

class SchoolPermissionGrantDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : SchoolPermissionGrantDataSource, SchoolUrlBasedDataSource {

    private suspend fun SchoolPermissionGrantDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(SchoolPermissionGrantDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
            }
            .build()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<SchoolPermissionGrant>> {
        return httpClient.getDataLoadResultAsFlow<List<SchoolPermissionGrant>>(
            urlFn = {
                SchoolPermissionGrantDataSource.GetListParams(
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

    override suspend fun findByGuid(params: DataLoadParams, guid: String): DataLoadState<SchoolPermissionGrant> {
        return httpClient.getAsDataLoadState<List<SchoolPermissionGrant>>(
            SchoolPermissionGrantDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolPermissionGrantDataSource.GetListParams,
    ): IPagingSourceFactory<Int, SchoolPermissionGrant> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<SchoolPermissionGrant>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                }
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolPermissionGrantDataSource.GetListParams,
    ): DataLoadState<List<SchoolPermissionGrant>> {
        return httpClient.getAsDataLoadState(
            url = params.urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<SchoolPermissionGrant>) {
        httpClient.post(
            respectEndpointUrl(SchoolPermissionGrantDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
