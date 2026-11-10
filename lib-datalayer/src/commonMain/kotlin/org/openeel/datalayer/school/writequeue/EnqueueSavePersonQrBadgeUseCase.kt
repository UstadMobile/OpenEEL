package org.openeel.datalayer.school.writequeue

import kotlinx.coroutines.flow.Flow

fun interface EnqueueSavePersonQrBadgeUseCase {

    data class Request(
        val personGuid: String,
        val qrCodeUrl: String,
    )

    enum class Status {
        QUEUED,
        RUNNING,
        SAVED,
        ALREADY_ASSIGNED,
        FAILED,
    }

    suspend operator fun invoke(request: Request): Flow<Status>
}
