package org.openeel.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
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
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.datalayer.school.model.PersonQrBadge
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource
import org.openeel.datalayer.shared.params.GetListCommonParams

class PersonQrBadgeDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonQrBadgeDataSource, SchoolUrlBasedDataSource {

    private suspend fun PersonQrBadgeDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(PersonQrBadgeDataSource.ENDPOINT_NAME)).apply {
            parameters.appendCommonListParams(common)
            parameters.appendIfNotNull(PersonQrBadgeDataSource.PARAM_QRCODE_URL, qrCodeUrl?.toString())
        }.build()
    }

    override suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: PersonQrBadgeDataSource.GetListParams
    ): DataLoadState<List<PersonQrBadge>> {
        return httpClient.getAsDataLoadState(
            url = listParams.urlWithParams(),
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrBadgeDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonQrBadge>>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = { listParams.urlWithParams() },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonQrBadge>> {
        return httpClient.getDataLoadResultAsFlow<List<PersonQrBadge>>(
            urlFn = {
                PersonQrBadgeDataSource.GetListParams(
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

    override suspend fun store(list: List<PersonQrBadge>) {
        httpClient.post(
            respectEndpointUrl(PersonQrBadgeDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }
}