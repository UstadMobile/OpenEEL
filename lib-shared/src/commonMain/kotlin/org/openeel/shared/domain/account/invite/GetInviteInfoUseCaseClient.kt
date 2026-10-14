package org.openeel.shared.domain.account.invite

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import org.openeel.datalayer.http.ext.respectEndpointUrl
import org.openeel.datalayer.http.school.SchoolUrlBasedDataSource
import org.openeel.datalayer.respect.model.invite.InviteInfo
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryResource

class GetInviteInfoUseCaseClient(
    override val schoolUrl: Url,
    override val schoolDirectoryEntryResource: SchoolDirectoryEntryResource,
    private val httpClient: HttpClient,
): GetInviteInfoUseCase, SchoolUrlBasedDataSource {

    override suspend fun invoke(code: String): InviteInfo {
        return httpClient.get(
            URLBuilder(respectEndpointUrl("invite/info"))
                .apply {
                    parameters.append("code", code)
                }.build()
        ).body()
    }
}
