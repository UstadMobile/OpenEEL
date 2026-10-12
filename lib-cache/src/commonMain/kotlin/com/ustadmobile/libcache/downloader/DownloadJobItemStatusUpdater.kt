package com.ustadmobile.libcache.downloader

import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.util.withWriterTransaction
import io.github.aakira.napier.Napier
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.openeel.libutil.ext.lastDistinctBy

/**
 * Update the status of DownloadJobItems in batches periodically.
 */
class DownloadJobItemStatusUpdater(
    private val db: UstadCacheDb,
    scope: CoroutineScope,
    private val commitInterval: Long = 500,
) {


    private val finished = atomic(false)

    private val progressUpdates = atomic(
        emptyList<DownloadProgressUpdate>()
    )
    private val statusUpdates = atomic(
        emptyList<DownloadStatusUpdate>()
    )

    private val updateJob = scope.launch {
        while(isActive) {
            delay(commitInterval)
            commit()
        }
    }


    fun onProgressUpdate(update: DownloadProgressUpdate) {
        progressUpdates.update { prev ->
            prev + update
        }
    }

    fun onStatusUpdate(update: DownloadStatusUpdate) {
        statusUpdates.update { prev ->
            prev + update
        }
    }

    /**
     * @param updateTransferJobStatusUid a transferjobuid for which we should set TransferJob.tjStatus to
     *        complete if all related TransferJobItem.tjiStatus(s) are complete
     *
     */
    suspend fun commit(
        updateTransferJobStatusUid: Int = 0
    ){
        val progressUpdatesToQueue = progressUpdates.getAndSet(emptyList())

        val statusUpdatesToQueue = statusUpdates.getAndSet(emptyList())

        val progressUpdatesToCommit = progressUpdatesToQueue.lastDistinctBy {
            it.jobItem.djiUid
        }
        val statusUpdatesToCommit = statusUpdatesToQueue.lastDistinctBy {
            it.jobItem.djiUid
        }

        db.takeIf {
            progressUpdatesToCommit.isNotEmpty() || statusUpdatesToCommit.isNotEmpty()
                    || updateTransferJobStatusUid != 0
        }?.withWriterTransaction {
            progressUpdatesToCommit.forEach {
                db.downloadJobItemDao.updateTransferredProgress(
                    jobItemUid = it.jobItem.djiUid,
                    transferred = it.bytesTransferred,
                )
            }

            statusUpdatesToCommit.forEach {
                db.downloadJobItemDao.updateStatus(
                    jobItemUid = it.jobItem.djiUid,
                    status = it.status
                )
            }

            if(updateTransferJobStatusUid != 0) {
                val numUpdates = db.downloadJobDao.updateStatusSetCompleteIfAllItemsComplete(
                    jobUid = updateTransferJobStatusUid
                )
                Napier.d { "TransferJobItemStatusUpdater: update status complete for " +
                        "$updateTransferJobStatusUid updates=$numUpdates" }
            }
        }
    }

    suspend fun onFinished(
        updateTransferJobStatusUid: Int = 0
    ) {
        if(!finished.getAndSet(true)) {
            updateJob.cancel()
            commit(updateTransferJobStatusUid)
        }
    }

}