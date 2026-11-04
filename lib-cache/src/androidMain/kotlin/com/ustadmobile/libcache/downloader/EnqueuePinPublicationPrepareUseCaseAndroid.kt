package com.ustadmobile.libcache.downloader

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.ustadmobile.libcache.db.UstadCacheDb
import com.ustadmobile.libcache.db.entities.DownloadJob
import io.ktor.http.Url
import org.openeel.libxxhash.XXStringHasher
import java.util.concurrent.TimeUnit

class EnqueuePinPublicationPrepareUseCaseAndroid(
    private val appContext: Context,
    db: UstadCacheDb,
    xxStringHasher: XXStringHasher,
) : AbstractEnqueuePinPublicationPrepareUseCase(db, xxStringHasher){

    override suspend fun invoke(manifestUrl: Url): DownloadJob {
        val transferJob = createTransferJob(manifestUrl)

        val jobData = Data.Builder()
            .putInt(JOB_UID, transferJob.djUid)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<PinPublicationPrepareUseCaseWorker>()
            .setInputData(jobData)
            .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
            .setConstraints(
                Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
            )
            .build()

        WorkManager.getInstance(appContext).enqueueUniqueWork(
            uniqueWorkName = "$UNIQUE_NAME_PREFIX-$manifestUrl",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            workRequest,
        )

        return transferJob
    }

    companion object {

        const val UNIQUE_NAME_PREFIX = "pin-getinfo"

        const val JOB_UID = "jobUid"

    }
}