package org.openeel.datalayer.repository.school.writequeue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import org.openeel.datalayer.repository.school.worker.getWorkerKoinScope
import org.openeel.lib.xapi.remotewritequeue.DrainXapiRemoteWriteQueueUseCase

class DrainXapiRemoteWriteQueueWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {

        val drainXapiRemoteWriteQueueUseCase: DrainXapiRemoteWriteQueueUseCase = getWorkerKoinScope().get()
        val logPrefix = "DrainXapiRemoteWriteQueueWorker"

        return try {
            drainXapiRemoteWriteQueueUseCase()
            Napier.d("$logPrefix completed successfully")
            Result.success()
        }catch(e: Throwable) {
            Napier.w("$logPrefix error", e)
            Result.failure()
        }
    }

}
