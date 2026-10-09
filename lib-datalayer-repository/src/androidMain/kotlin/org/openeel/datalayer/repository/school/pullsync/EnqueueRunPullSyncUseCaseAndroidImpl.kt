package world.respect.datalayer.repository.school.pullsync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import world.respect.datalayer.repository.school.worker.putKoinScope
import world.respect.datalayer.school.writequeue.EnqueueRunPullSyncUseCase
import kotlin.reflect.KClass

class EnqueueRunPullSyncUseCaseAndroidImpl(
    private val context: Context,
    private val scopeId: String,
    private val scopeClass: KClass<*>,
) : EnqueueRunPullSyncUseCase {

    override suspend fun invoke() {
        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName = "$UNIQUE_NAME_PREFIX-$scopeId",
            existingWorkPolicy = ExistingWorkPolicy.KEEP,
            request = OneTimeWorkRequestBuilder<RunPullSyncWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(Data.Builder().putKoinScope(scopeId, scopeClass).build())
                .build()
        )
    }

    companion object {

        const val UNIQUE_NAME_PREFIX = "runpullsync"
    }
}