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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.transformWhile
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
                    .putString(SavePersonQrBadgeWorker.KEY_QR_CODE_URL, request.qrCodeUrl)
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

        return workManager.getWorkInfoByIdFlow(workRequest.id)
            .mapNotNull { workInfo -> workInfo?.toSaveStatus() }
            .distinctUntilChanged()
            .transformWhile { status ->
                emit(status)
                status == EnqueueSavePersonQrBadgeUseCase.Status.QUEUED ||
                        status == EnqueueSavePersonQrBadgeUseCase.Status.RUNNING
            }
    }

    private fun WorkInfo.toSaveStatus(): EnqueueSavePersonQrBadgeUseCase.Status =
        when (state) {
            WorkInfo.State.ENQUEUED,
            WorkInfo.State.BLOCKED -> EnqueueSavePersonQrBadgeUseCase.Status.QUEUED
            WorkInfo.State.RUNNING -> EnqueueSavePersonQrBadgeUseCase.Status.RUNNING
            WorkInfo.State.SUCCEEDED -> when (outputData.getString(SavePersonQrBadgeWorker.KEY_OUTCOME)) {
                SavePersonQrBadgeWorker.OUTCOME_SAVED ->
                    EnqueueSavePersonQrBadgeUseCase.Status.SAVED
                SavePersonQrBadgeWorker.OUTCOME_ALREADY_ASSIGNED ->
                    EnqueueSavePersonQrBadgeUseCase.Status.ALREADY_ASSIGNED
                else -> EnqueueSavePersonQrBadgeUseCase.Status.FAILED
            }
            WorkInfo.State.FAILED,
            WorkInfo.State.CANCELLED -> EnqueueSavePersonQrBadgeUseCase.Status.FAILED
        }

    companion object {
        const val UNIQUE_WORK_PREFIX = "save-person-qr-badge"
    }
}
