package world.respect.shared.domain.account.passkey

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.Url
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import world.respect.credentials.passkey.model.AuthenticationResponseJSON
import world.respect.credentials.passkey.model.PasskeyVerifyResult
import world.respect.libutil.ext.appendEndpointSegments

class VerifyPasskeyUseCase(
    private val httpClient: HttpClient,
    private val json: Json,
) {
    suspend operator fun invoke(
        authenticationResponseJSON: AuthenticationResponseJSON,
        schoolUrl: Url,
        rpId: String?,
    ): PasskeyVerifyResult {
        val result = httpClient.post(
            schoolUrl.appendEndpointSegments("api/passkey/verifypasskey")
        ) {
            contentType(ContentType.Application.Json)
            setBody(authenticationResponseJSON)
            parameter("rpId", rpId)
        }.bodyAsText()
        Napier.d { "passkeyres : $result" }
        val passkeyVerifyResult = json.decodeFromString<PasskeyVerifyResult>(result)

        if (!passkeyVerifyResult.isVerified) {
            throw Exception("Account not found")
        }
        return passkeyVerifyResult
    }
}