package world.respect.shared.domain.account.username.checkusernameunique

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class CheckUsernameUniqueUseCaseClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
) : CheckUsernameUniqueUseCase, SchoolUrlBasedDataSource {

    override suspend fun invoke(username: String): Boolean {
        return httpClient.get(
            URLBuilder(
                respectEndpointUrl(CheckUsernameUniqueUseCase.ENDPOINT_PATH)
            ).apply {
                parameters.append(CheckUsernameUniqueUseCase.PARAM_USERNAME, username)
            }.build()
        ).body()
    }

}