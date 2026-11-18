package org.openeel.datalayer.repository.school.opds.crawlpublications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.openeel.datalayer.worker.getWorkerKoinScope
import org.openeel.datalayer.school.opds.crawlpublications.CrawlPublicationCollectionsUseCase

class CrawlPublicationCollectionsWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        return try {
            val workerKoinScope = getWorkerKoinScope()
            val crawlPublicationCollectionsUseCase: CrawlPublicationCollectionsUseCase =
                workerKoinScope.get()
            val jobId = inputData.getInt(KEY_JOB_ID, -1)

            crawlPublicationCollectionsUseCase(
                jobId = jobId,
                visitor = workerKoinScope.getOrNull(),
            )
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.w("CrawlPublicationCollectionsWorker error", e)
            Result.failure()
        }
    }

    companion object {

        const val KEY_JOB_ID = "publicationUrl"

    }

}