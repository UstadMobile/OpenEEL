package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.DownloadJob
import com.ustadmobile.libcache.db.entities.DownloadJobItem
import com.ustadmobile.libcache.db.entities.TransferJobItemStatus
import com.ustadmobile.libcache.util.withWriterTransaction
import io.ktor.http.Url
import org.openeel.libxxhash.XXStringHasher
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 *
 */
@OptIn(ExperimentalTime::class)
abstract class AbstractEnqueuePinPublicationPrepareUseCase(
    private val db: UstadCacheDb,
    private val xxStringHasher: XXStringHasher,
) : EnqueuePinPublicationPrepareUseCase {

    /**
     * @param manifestUrl the URL of the Readium Web Publication Manifest (that should
     */
    protected suspend fun createTransferJob(
        manifestUrl: Url,
    ): DownloadJob {
        return db.withWriterTransaction {
            val downloadJob = DownloadJob(
                djType = DownloadJob.TYPE_DOWNLOAD,
                djStatus = TransferJobItemStatus.STATUS_QUEUED_INT,
                djTimeCreated = Clock.System.now().toEpochMilliseconds(),
                djPubManifestUrl = manifestUrl,
                djPubManifestHash = xxStringHasher.hash(manifestUrl.toString()),
            )

            val jobUid = db.downloadJobDao.insert(downloadJob).toInt()

            val manifestDownloadJobItem = DownloadJobItem(
                djiDjUid = jobUid,
                djiUrl = manifestUrl,
            )

            db.downloadJobItemDao.upsertList(listOf(manifestDownloadJobItem))

            downloadJob.copy(
                djUid = jobUid
            )
        }
    }

}