package world.respect.shared.domain.account.passkey

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import world.respect.libutil.ext.appendEndpointSegments

class RevokePasskeyUseCaseClient(
    private val schoolUrl: Url,
    private val httpClient: HttpClient,
) : RevokePasskeyUseCase {


    override suspend fun invoke(personGuid: String) {
        return httpClient.post(
            schoolUrl.appendEndpointSegments("api/passkey/revokepasskey")
        ) {
            contentType(ContentType.Application.Json)
            parameter("personGuid", personGuid)
        }.body()
    }
}