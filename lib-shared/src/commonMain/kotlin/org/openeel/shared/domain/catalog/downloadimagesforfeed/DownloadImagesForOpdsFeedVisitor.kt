package org.openeel.shared.domain.catalog.downloadimagesforfeed

import io.github.aakira.napier.Napier
import io.github.reactivecircus.cache4k.Cache
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsChannel
import io.ktor.http.Url
import io.ktor.utils.io.discard
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationCollectionsUseCase
import org.openeel.datalayer.school.opds.ext.findIcons
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

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onVisit(feed: DataLoadState<OpdsFeed>) = coroutineScope {
        if (feed !is DataReadyState) {
            return@coroutineScope
        }

        val feedUrl = feed.metaInfo.requireUrl()
        val allImageLinks = (
            feed.data.allNavigationLinks().flatMap {
                it.findIcons()
            } + feed.data.allPublications().flatMap {
                it.images.orEmpty()
            }
        ).distinctBy { feedUrl.resolve(it.href) }

        val producer = produce(capacity = DOWNLOAD_WORKER_COUNT) {
            allImageLinks.forEach { send(it) }
            close()
        }

        repeat(DOWNLOAD_WORKER_COUNT) {
            launch {
                for (imageLink in producer) {
                    val imageUrl = feedUrl.resolve(imageLink.href)
                    try {
                        imageCache.get(imageUrl) {
                            httpClient.get(imageUrl).also { response ->
                                response.bodyAsChannel().discard()
                            }.status.value
                        }
                    } catch (e: CancellationException) {
                        throw e
                    } catch (e: Throwable) {
                        Napier.w(
                            message = "DownloadImagesForOpdsFeedVisitor: failure downloading image:" +
                                " ${imageLink.href} for $feedUrl",
                            throwable = e,
                        )
                    }
                }
            }
        }
    }

    companion object {
        internal const val DOWNLOAD_WORKER_COUNT = 5
    }
}