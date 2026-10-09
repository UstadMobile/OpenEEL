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
import world.respect.datalayer.http.ext.appendCommonListParams
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.datalayer.school.model.PersonPassword
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class PersonPasswordDataSourceHttpClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val tokenProvider: AuthTokenProvider,
    private val validationHelper: ExtendedDataSourceValidationHelper?,
) : PersonPasswordDataSource, SchoolUrlBasedDataSource{

    private suspend fun PersonPasswordDataSource.GetListParams.urlWithParams(): Url {
        return URLBuilder(respectEndpointUrl(PersonPasswordDataSource.ENDPOINT_NAME)).apply {
            parameters.appendCommonListParams(common)
        }.build()
    }

    override suspend fun listAll(listParams: PersonPasswordDataSource.GetListParams): DataLoadState<List<PersonPassword>> {
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
        listParams: PersonPasswordDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonPassword>>> {
        return httpClient.getDataLoadResultAsFlow(
            urlFn = { listParams.urlWithParams() },
            dataLoadParams = loadParams,
            validationHelper = validationHelper,
        ) {
            useTokenProvider(tokenProvider)
            useValidationCacheControl(validationHelper)
        }
    }


    override suspend fun store(list: List<PersonPassword>) {
        httpClient.post(
            respectEndpointUrl(PersonPasswordDataSource.ENDPOINT_NAME)
        ) {
            useTokenProvider(tokenProvider)
            contentType(ContentType.Application.Json)
            setBody(list)
        }
    }

}