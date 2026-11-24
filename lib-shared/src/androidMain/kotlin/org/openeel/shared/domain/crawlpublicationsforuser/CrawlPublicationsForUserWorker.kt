package org.openeel.shared.domain.crawlpublicationsforuser

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.openeel.datalayer.worker.getWorkerKoinScope

class CrawlPublicationsForUserWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        return try {
            val workerKoinScope = getWorkerKoinScope()
            val crawlPublicationsForUserUseCase: CrawlPublicationsForUserUseCase = workerKoinScope.get()

            crawlPublicationsForUserUseCase()
            Result.success()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.w("CrawlPublicationsForUserWorker error", e)
            Result.failure()
        }
    }

}