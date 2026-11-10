package org.openeel.shared.domain.school

import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

class GetHostListUseCase(
    private val httpClient: HttpClient,
    private val json: Json,
    private val hostListUrl: Url,
) {

    @Serializable
    data class Host(
        val baseUrl: Url,
        val icon: String,
    )

    suspend operator fun invoke(): List<Host> =
        json.decodeFromString<List<Host>>(
            httpClient.get(hostListUrl) {
                expectSuccess = true
                header(HttpHeaders.CacheControl, "no-cache, no-store")
            }.bodyAsText()
        )
}
