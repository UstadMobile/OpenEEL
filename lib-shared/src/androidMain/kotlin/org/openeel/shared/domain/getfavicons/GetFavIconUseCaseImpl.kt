package org.openeel.shared.domain.getfavicons

import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.HttpClient
import io.ktor.client.plugins.expectSuccess
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.client.statement.request
import io.ktor.http.Url
import org.jsoup.Jsoup
import org.openeel.domain.getfavicons.GetFavIconUseCase
import org.openeel.libutil.ext.resolve

class GetFavIconUseCaseImpl(
    private val httpClient: HttpClient,
) : GetFavIconUseCase {

    private val cache = Cache.Builder<Url, List<GetFavIconUseCase.FavIcon>>().build()

    override suspend fun invoke(url: Url): List<GetFavIconUseCase.FavIcon> = cache.get(url) {
        val response = httpClient.get(url) { expectSuccess = true }
        val document = Jsoup.parse(response.bodyAsText(), response.request.url.toString())

        document.select("link[href][rel~=(?i)(^|\\s)icon(\\s|$)]").map { link ->
            val sizes = link.attr("sizes").ifBlank { null }
            val dimensions = sizes?.split("x", limit = 2)

            GetFavIconUseCase.FavIcon(
                url = link.absUrl("href"),
                type = link.attr("type").ifBlank { null },
                sizes = sizes,
                width = dimensions?.firstOrNull()?.toIntOrNull(),
                height = dimensions?.getOrNull(1)?.toIntOrNull(),
            )
        }.filter { it.url.isNotBlank() }.ifEmpty {
            listOf(
                GetFavIconUseCase.FavIcon(
                    url = response.request.url.resolve("/favicon.ico").toString(),
                    type = null,
                    sizes = null,
                    width = null,
                    height = null,
                )
            )
        }
    }
}