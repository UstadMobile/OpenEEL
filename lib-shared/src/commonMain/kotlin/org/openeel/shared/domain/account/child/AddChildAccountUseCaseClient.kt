package world.respect.shared.domain.account.child

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.datalayer.AuthTokenProvider
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.ext.useTokenProvider
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class AddChildAccountUseCaseClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
    private val authTokenProvider: AuthTokenProvider,
    private val schoolDataSourceLocal: SchoolDataSourceLocal,
) : AddChildAccountUseCase, SchoolUrlBasedDataSource {

    override suspend fun invoke(
        request: AddChildAccountUseCase.AddChildAccountRequest
    ): AddChildAccountUseCase.AddChildAccountResponse {
        return httpClient.post(
            URLBuilder(
                respectEndpointUrl(AddChildAccountUseCase.ENDPOINT_NAME)
            ).build()
        ) {
            useTokenProvider(authTokenProvider)
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body<AddChildAccountUseCase.AddChildAccountResponse>().also { addChildResponse ->
            schoolDataSourceLocal.personDataSource.updateLocal(
                listOf(addChildResponse.parentPerson, addChildResponse.childPerson)
            )
        }
    }

}