package org.openeel.datalayer.school.writequeue

import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow

fun interface EnqueueSavePersonQrBadgeUseCase {

    data class Request(
        val personGuid: String,
        val qrCodeUrl: Url,
    )

    enum class Status {
        SAVED,
        ALREADY_ASSIGNED,
        FAILED,
    }

    suspend operator fun invoke(request: Request): Flow<Status>
}
