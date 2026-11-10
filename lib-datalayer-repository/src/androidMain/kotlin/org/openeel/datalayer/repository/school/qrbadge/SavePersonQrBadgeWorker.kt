package org.openeel.datalayer.repository.school.qrbadge

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import io.github.aakira.napier.Napier
import io.ktor.http.Url
import kotlinx.coroutines.CancellationException
import org.koin.core.component.KoinComponent
import org.openeel.datalayer.repository.school.worker.getWorkerKoinScope

class SavePersonQrBadgeWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params), KoinComponent {

    override suspend fun doWork(): Result {
        val savePersonQrBadgeUseCase: SavePersonQrBadgeUseCase = getWorkerKoinScope().get()

        return try {
            val personGuid = inputData.getString(KEY_PERSON_GUID)
                ?: error("Missing QR badge person GUID")
            val qrCodeUrl = inputData.getString(KEY_QR_CODE_URL)?.let(::Url)
                ?: error("Missing QR badge URL")

            val outcome = savePersonQrBadgeUseCase(
                SavePersonQrBadgeUseCase.Request(
                    personGuid = personGuid,
                    qrCodeUrl = qrCodeUrl,
                )
            )
            when (outcome) {
                SavePersonQrBadgeUseCase.Outcome.SAVED -> Result.success()
                SavePersonQrBadgeUseCase.Outcome.ALREADY_ASSIGNED -> Result.failure(
                    Data.Builder()
                        .putString(KEY_OUTCOME, OUTCOME_ALREADY_ASSIGNED)
                        .build()
                )
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Napier.w("SavePersonQrBadgeWorker error", e)
            Result.failure()
        }
    }

    companion object {
        const val KEY_PERSON_GUID = "personGuid"
        const val KEY_QR_CODE_URL = "qrCodeUrl"
        const val KEY_OUTCOME = "outcome"
        const val OUTCOME_ALREADY_ASSIGNED = "already_assigned"
    }
}
