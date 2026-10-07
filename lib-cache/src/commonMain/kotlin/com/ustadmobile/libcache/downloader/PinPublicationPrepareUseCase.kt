package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.EntryLockRequest
import com.ustadmobile.libcache.UstadCache
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.DownloadJobItem
import com.ustadmobile.libcache.db.entities.PinnedPublication
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import com.ustadmobile.libcache.util.LaunchNoVarySearchConstants
import com.ustadmobile.libcache.util.isHttp
import com.ustadmobile.libcache.util.withWriterTransaction
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.contentLength
import io.ktor.http.headersOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.coroutineScope
import nl.adaptivity.xmlutil.serialization.XML
import org.openeel.lib.opds.model.Publication
import org.openeel.lib.opds.model.ReadiumLink
import org.openeel.lib.opds.model.findLearningUnitAcquisitionLinks
import org.openeel.lib.opds.model.findTinCanXmlLink
import org.openeel.lib.xapi.rusticilaunch.model.TinCanXmlDocument
import org.openeel.libutil.ext.resolve
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Fetches the publication manifest (over http) and goes through all of the resource links.
 * For each resource:
 *
 *  a) Create a DownloadJobItem
 *  b) Add a RetentionLock
 *  c) If the resource does not have a size provided in the ReadiumLink, then make an HTTP HEAD request
 *     to get the size and update the TransferJobItem accordingly.
 *  d) Enqueue RunDownloadJob to run the actual download.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PinPublicationPrepareUseCase(
    private val httpClient: HttpClient,
    private val db: UstadCacheDb,
    private val cache: UstadCache,
    private val enqueueRunDownloadJobUseCase: EnqueueRunDownloadJobUseCase,
    private val xml: XML,
) {


    /**
     * This is here because some manifests contain spaces in hrefwhich should NOT be there.
     */
    fun String.cleanHref(): String {
        return this.replace(" ", "%20")
    }

    /**
     *
     */
    suspend operator fun invoke(
        downloadJobUid: Int
    ) {
        val downloadJob = db.downloadJobDao.findByUid(downloadJobUid)
            ?: throw IllegalArgumentException("No transfer job with uid $downloadJobUid")
        val manifestJobItem = db.downloadJobItemDao.findPendingByJobUid(downloadJobUid).first()
        db.downloadJobDao.updateStatus(
            jobUid = downloadJobUid,
            status = TransferJobItemStatus.STATUS_PREPARING_INT,
        )

        val manifestUrl = downloadJob.djPubManifestUrl
            ?: throw IllegalArgumentException("no manifest url")

        val publication: Publication = httpClient.get(manifestUrl).body()
        val tinCanXmlLink = publication.findTinCanXmlLink()
        val tinCanXmlUrl = tinCanXmlLink?.href?.let { manifestUrl.resolve(it) }
        val tinCanXmlDocument = tinCanXmlUrl?.let {
            val xmlStr = httpClient.get(it).bodyAsText()
            xml.decodeFromString(TinCanXmlDocument.serializer(), xmlStr)
        }

        val tinCanLaunchUrls = tinCanXmlDocument?.activities?.activity?.mapNotNull { activity ->
            activity.launch?.value?.let { tinCanXmlUrl.resolve(it) }
        }?.filter {
            it.protocol.isHttp()
        } ?: emptyList()

        val resourceAndAcquireJobItems = buildList<DownloadJobItem> {
            val acquisitionLinks = publication.findLearningUnitAcquisitionLinks()
            val acquisitionLinksUrls = acquisitionLinks.map {
                manifestUrl.resolve(it.href)
            }

            /*
             * For any url that might be launched with xAPI Launch Parameters, set no vary search
             * header. If this is not set the variance in the xAPI launch parameters will cause lead
             * to a cache miss and offline loading will fail.
             */
            (acquisitionLinksUrls + tinCanLaunchUrls).distinct().forEach { launchableUrl ->
                cache.setExtraResponseHeaders(
                    url = launchableUrl,
                    extraResponseHeaders = headersOf(
                        "No-Vary-Search" to listOf(LaunchNoVarySearchConstants.LAUNCH_LINK_NO_VARY_HEADER)
                    )
                )
            }

            val linksToDownload: List<ReadiumLink> = buildList {
                tinCanXmlLink?.also { add(it) }
                addAll(
                    tinCanLaunchUrls.map { ReadiumLink(href = it.toString()) }
                )

                publication.resources?.also { addAll(it) }
                addAll(acquisitionLinks)
                add(ReadiumLink(href = manifestUrl.toString()))
                publication.images?.also { addAll(it) }
            }

            addAll(
                linksToDownload.map { resource ->
                    DownloadJobItem(
                        djiDjUid = downloadJobUid,
                        djiUrl = manifestUrl.resolve(resource.href.cleanHref()),
                        djiTotalSize = (resource.size ?: 0).toLong()
                    )
                }
            )

            add(manifestJobItem)
        }.distinctBy { item -> item.djiUrl }

        val downloadJobItemWithSize: MutableList<DownloadJobItem> = CopyOnWriteArrayList()

        coroutineScope {
            val jobItemProducer = produce(
                capacity = Channel.UNLIMITED
            ) {
                resourceAndAcquireJobItems.forEach { send(it) }
                close()
            }

            val jobs = (1.. PARALLEL_SIZE_FETCH_LIMIT).map {
                async {
                    for (item in jobItemProducer) {
                        val jobItemWithSize = if(item.djiTotalSize <= 0) {
                            val headResponse = httpClient.head(item.djiUrl) {
                                header("cache-control", "no-cache, no-store")
                            }
                            val contentLength = headResponse.contentLength()

                            item.copy(
                                djiTotalSize = contentLength ?: 0
                            )
                        }else {
                            item
                        }
                        downloadJobItemWithSize.add(jobItemWithSize)
                    }
                }
            }

            jobs.awaitAll()
        }

        cache.addRetentionLocks(
            downloadJobItemWithSize.map {
                EntryLockRequest(
                    url = it.djiUrl.toString(),
                    publicationUid = downloadJob.djPubManifestHash,
                )
            }
        )

        db.withWriterTransaction {
            db.downloadJobItemDao.upsertList(downloadJobItemWithSize)
            db.pinnedPublicationDao.insert(
                PinnedPublication(
                    ppUrlHash = downloadJob.djPubManifestHash,
                    title = downloadJob.djName ?: ""
                )
            )
        }

        enqueueRunDownloadJobUseCase(downloadJobUid)
    }


    companion object {

        const val PARALLEL_SIZE_FETCH_LIMIT = 8

        const val DEFAULT_MAX_ATTEMPTS = 5

    }
}