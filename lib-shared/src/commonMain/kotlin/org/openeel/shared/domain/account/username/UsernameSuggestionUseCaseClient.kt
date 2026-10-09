package world.respect.shared.domain.account.username

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class UsernameSuggestionUseCaseClient (
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
) : UsernameSuggestionUseCase , SchoolUrlBasedDataSource {


    override suspend fun invoke(name: String): String {
        return httpClient.post(
            URLBuilder(respectEndpointUrl("username/getsuggestion"))
                .apply {
                    parameters.append("name", name)
                }.build()
        ).body()
    }
}