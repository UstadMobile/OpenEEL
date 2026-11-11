package org.openeel.datalayer.repository.school.qrbadge

import io.ktor.http.Url
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.datalayer.school.model.PersonQrBadge
import org.openeel.datalayer.school.model.StatusEnum
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.dataOrNull
import kotlin.time.Clock

class SavePersonQrBadgeUseCase(
    private val personQrBadgeDataSource: PersonQrBadgeDataSource,
) {

    enum class Outcome {
        SAVED,
        ALREADY_ASSIGNED,
    }

    data class Request(
        val personGuid: String,
        val qrCodeUrl: Url,
    )

    suspend operator fun invoke(request: Request): Outcome {
        val matchingBadges = personQrBadgeDataSource.listAll(
            loadParams = DataLoadParams(),
            listParams = PersonQrBadgeDataSource.GetListParams(
                qrCodeUrl = request.qrCodeUrl,
            ),
        ).dataOrNull() ?: error("Unable to check whether the QR badge is already assigned")

        if (matchingBadges.any { it.personGuid != request.personGuid }) {
            return Outcome.ALREADY_ASSIGNED
        }

        personQrBadgeDataSource.store(
            listOf(
                PersonQrBadge(
                    personGuid = request.personGuid,
                    qrCodeUrl = request.qrCodeUrl,
                    lastModified = Clock.System.now(),
                    status = StatusEnum.ACTIVE,
                )
            )
        )
        return Outcome.SAVED
    }
}
