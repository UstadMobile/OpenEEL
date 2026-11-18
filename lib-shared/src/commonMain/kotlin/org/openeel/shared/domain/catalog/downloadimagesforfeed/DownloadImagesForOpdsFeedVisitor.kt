package org.openeel.shared.domain.catalog.downloadimagesforfeed

import io.github.aakira.napier.Napier
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.utils.io.discard
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationCollectionsUseCase
import org.openeel.datalayer.school.opds.ext.findIcons
import org.openeel.datalayer.school.opds.ext.requireSelfUrl
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.opds.model.OpdsFeed
import org.openeel.lib.opds.model.ext.allNavigationLinks
import org.openeel.lib.opds.model.ext.allPublications
import org.openeel.libutil.ext.resolve

/**
 * Implementation of [CrawlPublicationCollectionsUseCase.OpdsFeedVisitor] that will fetch all images
 * e.g. to pull them through the cache so that thumbnails will be visible if/when the user is
 * browsing offline later.
 */
class DownloadImagesForOpdsFeedVisitor(
    private val httpClient: HttpClient,
) : CrawlPublicationCollectionsUseCase.OpdsFeedVisitor {

    private val imageCache = Cache.Builder<Url, Int>().build()

    override suspend fun onVisit(feed: DataLoadState<OpdsFeed>) {
        if(feed is DataReadyState) {
            val allImageLinks = feed.data.allNavigationLinks().flatMap {
                it.findIcons()
            } + feed.data.allPublications().flatMap {
                it.images.orEmpty()
            }.distinctBy { it.href }

            val feedUrl = feed.data.requireSelfUrl()

            allImageLinks.forEach {
                try {
                    val imageUrl = feedUrl.resolve(it.href)
                    imageCache.get(imageUrl) {
                        httpClient.get(imageUrl).also { response ->
                            response.bodyAsChannel().discard()
                        }.status.value
                    }
                }catch(e: Throwable) {
                    Napier.w(
                        message = "DownloadImagesForOpdsFeedVisitor: failure downloading image:" +
                            " ${it.href} for $feedUrl",
                        throwable = e,
                    )
                }
            }
        }
    }
}