package org.openeel.datalayer.repository.school.qrbadge

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.take
import org.openeel.datalayer.repository.school.worker.putKoinScope
import org.openeel.datalayer.school.writequeue.EnqueueSavePersonQrBadgeUseCase
import kotlin.reflect.KClass

class EnqueueSavePersonQrBadgeUseCaseAndroidImpl(
    context: Context,
    private val scopeId: String,
    private val scopeClass: KClass<*>,
) : EnqueueSavePersonQrBadgeUseCase {

    private val workManager = WorkManager.getInstance(context)

    override suspend fun invoke(
        request: EnqueueSavePersonQrBadgeUseCase.Request
    ): Flow<EnqueueSavePersonQrBadgeUseCase.Status> {
        val workRequest = OneTimeWorkRequestBuilder<SavePersonQrBadgeWorker>()
            .setInputData(
                Data.Builder()
                    .putKoinScope(scopeId, scopeClass)
                    .putString(SavePersonQrBadgeWorker.KEY_PERSON_GUID, request.personGuid)
                    .putString(SavePersonQrBadgeWorker.KEY_QR_CODE_URL, request.qrCodeUrl.toString())
                    .build()
            )
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()

        workManager.enqueueUniqueWork(
            uniqueWorkName = "$UNIQUE_WORK_PREFIX-$scopeId-${request.personGuid}",
            existingWorkPolicy = ExistingWorkPolicy.APPEND_OR_REPLACE,
            request = workRequest,
        )

        // Ignore non-terminal WorkInfo updates and emit only the first completed outcome.
        return workManager.getWorkInfoByIdFlow(workRequest.id)
            .mapNotNull { workInfo -> workInfo?.toSaveStatus() }
            .take(1)
    }

    private fun WorkInfo.toSaveStatus(): EnqueueSavePersonQrBadgeUseCase.Status? =
        when (state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.BLOCKED,
            // These are in-progress states: wait for WorkManager's next update.
            WorkInfo.State.RUNNING -> null
            WorkInfo.State.SUCCEEDED -> EnqueueSavePersonQrBadgeUseCase.Status.SAVED
            WorkInfo.State.FAILED -> when (
                outputData.getString(SavePersonQrBadgeWorker.KEY_OUTCOME)
            ) {
                SavePersonQrBadgeWorker.OUTCOME_ALREADY_ASSIGNED ->
                    EnqueueSavePersonQrBadgeUseCase.Status.ALREADY_ASSIGNED
                else -> EnqueueSavePersonQrBadgeUseCase.Status.FAILED
            }
            WorkInfo.State.CANCELLED -> EnqueueSavePersonQrBadgeUseCase.Status.FAILED
        }

    companion object {
        const val UNIQUE_WORK_PREFIX = "save-person-qr-badge"
    }
}
