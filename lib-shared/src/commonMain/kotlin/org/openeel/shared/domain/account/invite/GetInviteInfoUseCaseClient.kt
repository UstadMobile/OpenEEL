package world.respect.shared.domain.account.invite

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import world.respect.datalayer.http.ext.respectEndpointUrl
import world.respect.datalayer.http.school.SchoolUrlBasedDataSource
import world.respect.datalayer.respect.model.invite.RespectInviteInfo
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource

class GetInviteInfoUseCaseClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSource,
    private val httpClient: HttpClient,
): GetInviteInfoUseCase, SchoolUrlBasedDataSource {

    override suspend fun invoke(code: String): RespectInviteInfo {
        return httpClient.get(
            URLBuilder(respectEndpointUrl("invite/info"))
                .apply {
                    parameters.append("code", code)
                }.build()
        ).body()
    }
}
