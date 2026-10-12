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
import org.openeel.datalayer.school.InviteDataSource
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.params.GetListCommonParams

class InviteDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : InviteDataSource, SchoolUrlBasedDataSource {

    private suspend fun InviteDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(InviteDataSource.ENDPOINT_NAME))
            .apply {
                parameters.appendCommonListParams(common)
                parameters.appendIfNotNull(InviteDataSource.PARAM_NAME_INVITE_CODE, inviteCode)
            }
            .build()
    }

    override suspend fun findByGuid(guid: String): DataLoadState<Invite2>{
        return httpClient.getAsDataLoadState<List<Invite2>>(
            InviteDataSource.GetListParams(
                GetListCommonParams(guid = guid)
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override fun findByUidAsFlow(
        uid: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<Invite2>> {
        return httpClient.getDataLoadResultAsFlow<List<Invite2>>(
            urlFn = {
                InviteDataSource.GetListParams(
                    GetListCommonParams(guid = uid)
                ).urlWithParams()
            },
            dataLoadParams = loadParams
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.map {
            it.firstOrNotLoaded()
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: InviteDataSource.GetListParams
    ): IPagingSourceFactory<Int, Invite2> {
        return IPagingSourceFactory {
            OffsetLimitHttpPagingSource(
                baseUrlProvider = { params.urlWithParams() },
                httpClient = httpClient,
                validationHelper = validationHelper,
                typeInfo = typeInfo<List<Invite2>>(),
                requestBuilder = {
                    useTokenProvider(tokenProvider)
                    useValidationCacheControl(validationHelper)
                }
            )
        }
    }

    override suspend fun findByCode(code: String): DataLoadState<Invite2> {
        return httpClient.getAsDataLoadState<List<Invite2>>(
            InviteDataSource.GetListParams(
                inviteCode = code
            ).urlWithParams()
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }.firstOrNotLoaded()
    }

    override suspend fun store(list: List<Invite2>) {
        httpClient.post(
            url = respectEndpointUrl(InviteDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}
