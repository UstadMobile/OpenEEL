package org.openeel.datalayer.repository.school.opds.crawlpublications

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import org.openeel.datalayer.worker.putKoinScope
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueue
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationQueueItem
import org.openeel.datalayer.school.opds.crawlpublications.EnqueueCrawlPublicationCollectionUseCase
import kotlin.reflect.KClass

class EnqueueCrawlPublicationCollectionUseCaseAndroidImpl(
    private val context: Context,
    private val scopeId: String,
    private val scopeClass: KClass<*>,
    private val crawlPublicationQueue: CrawlPublicationQueue,
) : EnqueueCrawlPublicationCollectionUseCase {

    override suspend fun invoke(
        request: EnqueueCrawlPublicationCollectionUseCase.Request,
    ) {
        val jobId = crawlPublicationQueue.insertItemsIfNotInJob(
            items = listOf(
                CrawlPublicationQueueItem(
                    jobId = 0,
                    itemUrl = request.url,
                )
            ),
            newJob = true,
        )

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName = "$UNIQUE_NAME_PREFIX-$scopeId-${request.url}",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = OneTimeWorkRequestBuilder<CrawlPublicationCollectionsWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data.Builder()
                        .putKoinScope(scopeId, scopeClass)
                        .putInt(CrawlPublicationCollectionsWorker.KEY_JOB_ID, jobId)
                        .build()
                )
                .build()
        )
    }

    companion object {

        const val UNIQUE_NAME_PREFIX = "crawlpublicationcollection"

    }

}