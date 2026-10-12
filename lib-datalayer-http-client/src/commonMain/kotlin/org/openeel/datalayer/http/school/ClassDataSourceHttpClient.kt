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
import org.openeel.datalayer.http.ext.appendIfNotNull
import org.openeel.datalayer.http.ext.respectEndpointUrl
import org.openeel.datalayer.http.shared.paging.OffsetLimitHttpPagingSource
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.ClassDataSource.Companion.PARAM_NAME_INVITE_CODE
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

class ClassDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : ClassDataSource, SchoolUrlBasedDataSource {

    private suspend fun ClassDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(ClassDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(PARAM_NAME_INVITE_CODE, inviteGuid)
            }
            .build()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>> {
        return httpClient.getDataLoadResultAsFlow<List<Clazz>>(
            urlFn = {
                ClassDataSource.GetListParams(
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

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz> {
        return httpClient.getAsDataLoadState<List<Clazz>>(
            ClassDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): IPagingSourceFactory<Int, Clazz> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Clazz>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                }
            )
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): DataLoadState<List<Clazz>> {
        return httpClient.getAsDataLoadState(
            url = params.urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<Clazz>) {
        httpClient.post(
            respectEndpointUrl(ClassDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}