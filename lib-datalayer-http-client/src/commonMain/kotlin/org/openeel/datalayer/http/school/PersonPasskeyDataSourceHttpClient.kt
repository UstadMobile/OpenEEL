package world.respect.datalayer.http.school

import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.AuthTokenProvider
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.datalayer.ext.getAsDataLoadState
import world.respect.datalayer.ext.getDataLoadResultAsFlow
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.ext.useValidationCacheControl
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonPasskeyDataSource
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

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