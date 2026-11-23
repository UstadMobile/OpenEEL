package org.openeel.shared.domain.catalog.prefetchopdsfeedvisitor

import com.ustadmobile.libcache.EntryLockRequest
import com.ustadmobile.libcache.UstadCache
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
import org.openeel.lib.opds.model.findTinCanXmlLink
import org.openeel.shared.util.ext.resolve

/**
 * Implementation of [CrawlPublicationCollectionsUseCase.OpdsFeedVisitor] that will fetch all images
 * e.g. to pull them through the cache so that thumbnails will be visible if/when the user is
 * browsing offline later and fetch the tincan.xml file for any publication such that the publication
 * can be launched offline (tincan.xml is required to determine the xAPI activity id and href to
 * launch).
 *
 * It will also add retention locks to the images and tincan.xml files as per
 * [UstadCache.addRetentionLocks] to ensure they are retained.
 */
class PrefetchOpdsFeedVisitor(
    private val httpClient: HttpClient,
    private val cache: UstadCache,
) : CrawlPublicationCollectionsUseCase.OpdsFeedVisitor {

    private val prefertchedUrls = Cache.Builder<Url, Int>().build()

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun onVisit(feed: DataLoadState<OpdsFeed>) = coroutineScope {
        if (feed !is DataReadyState) {
            return@coroutineScope
        }

        val feedUrl = feed.metaInfo.requireUrl()

        val linksToPrefetch = (
            feed.data.allNavigationLinks().flatMap {
                it.findIcons()
            } + feed.data.allPublications().flatMap {
                it.images.orEmpty()
            } + feed.data.allPublications().mapNotNull {
                it.findTinCanXmlLink()
            }
        ).map {
            it.resolve(feedUrl)
        }.distinctBy { it.href }

        val producer = produce(capacity = DOWNLOAD_WORKER_COUNT) {
            linksToPrefetch.forEach { send(it) }
            close()
        }

        cache.addRetentionLocks(
            locks = linksToPrefetch.map {
                EntryLockRequest(url = it.href)
            }
        )

        repeat(DOWNLOAD_WORKER_COUNT) {
            launch {
                for (imageLink in producer) {
                    //Href is already resolved
                    val imageUrl = Url(imageLink.href)
                    try {
                        prefertchedUrls.get(imageUrl) {
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