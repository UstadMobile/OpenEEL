package world.respect.shared.domain.openexternallink

import io.ktor.http.Url

interface OpenExternalLinkUseCase {

    data class OpenLinkRequest(
        val url: Url
    )

    suspend operator fun invoke(request: OpenLinkRequest)

    suspend operator fun invoke(url: Url) = invoke(OpenLinkRequest(url = url))

}