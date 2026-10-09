package org.openeel.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.AuthTokenProvider
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.datalayer.ext.getAsDataLoadState
import org.openeel.datalayer.ext.getDataLoadResultAsFlow
import org.openeel.datalayer.ext.useTokenProvider
import org.openeel.datalayer.ext.useValidationCacheControl
import org.openeel.datalayer.http.ext.respectEndpointUrl
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.PersonPasskeyDataSource
import org.openeel.datalayer.school.model.PersonPasskey
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class PersonPasskeyDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonPasskeyDataSource, SchoolUrlBasedDataSource {

    private suspend fun PersonPasskeyDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(
            respectEndpointUrl(PersonPasskeyDataSource.ENDPOINT_NAME)
        ).apply {
            parameters.append(
                PersonPasskeyDataSource.PARAM_INCLUDE_REVOKED,
                includeRevoked.toString()
            )
        }.build()
    }


    override suspend fun listAll(
        listParams: PersonPasskeyDataSource.GetListParams
    ): DataLoadState<List<PersonPasskey>> {
        return httpClient.getAsDataLoadState<List<PersonPasskey>>(
            url = listParams.urlWithParams(),
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override fun listAllAsFlow(
        listParams: PersonPasskeyDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonPasskey>>> {
        return httpClient.getDataLoadResultAsFlow<List<PersonPasskey>>(
            urlFn = { listParams.urlWithParams() },
            dataLoadParams = DataLoadParams(),
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }

    override suspend fun store(list: List<PersonPasskey>) {
        httpClient.post(respectEndpointUrl(PersonPasskeyDataSource.ENDPOINT_NAME)) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }

}