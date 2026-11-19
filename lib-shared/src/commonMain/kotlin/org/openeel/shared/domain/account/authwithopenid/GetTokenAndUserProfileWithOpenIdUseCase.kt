package org.openeel.shared.domain.account.authwithopenid

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.appendPathSegments
import io.ktor.http.takeFrom
import org.openeel.shared.domain.account.AuthResponse

class GetTokenAndUserProfileWithOpenIdUseCase(
    private val httpClient: HttpClient,
) {

    suspend operator fun invoke(
        schoolUrl: Url,
        accessToken: String,
    ): AuthResponse = httpClient.post {
        url {
            takeFrom(schoolUrl)
            appendPathSegments("api/oidc/login")
        }
        header(HttpHeaders.Authorization, "Bearer $accessToken")
    }.body()
}
