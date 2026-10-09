package world.respect.datalayer.repository.school.pullsync

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import org.koin.core.component.KoinComponent
import world.respect.datalayer.repository.school.worker.getWorkerKoinScope

class RunPullSyncWorker(
    context: Context,
    params: WorkerParameters
): CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val runPullSyncUseCase: RunPullSyncUseCase = getWorkerKoinScope().get()

        return try {
            runPullSyncUseCase()
            Napier.d("RunPullSyncWorker completed successfully")
            Result.success()
        }catch(e: Throwable) {
            Napier.w("RunPullSyncWorker error", e)
            Result.failure()
        }
    }

}