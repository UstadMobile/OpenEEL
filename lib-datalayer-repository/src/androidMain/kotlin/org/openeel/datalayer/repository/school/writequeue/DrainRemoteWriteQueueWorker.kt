package world.respect.datalayer.repository.school.writequeue

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import world.respect.datalayer.repository.school.worker.getWorkerKoinScope

class DrainRemoteWriteQueueWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {

        val drainRemoteWriteQueueUseCase: DrainRemoteWriteQueueUseCase = getWorkerKoinScope().get()
        val logPrefix = "DrainRemoteWriteQueueWorker"

        return try {
            drainRemoteWriteQueueUseCase()
            Napier.d("$logPrefix completed successfully")
            Result.success()
        }catch(e: Throwable) {
            Napier.w("$logPrefix error", e)
            Result.failure()
        }
    }

}