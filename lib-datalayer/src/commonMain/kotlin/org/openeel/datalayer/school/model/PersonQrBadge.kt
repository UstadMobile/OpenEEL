package world.respect.datalayer.school.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import world.respect.datalayer.shared.ModelWithTimes
import world.respect.lib.serializers.InstantAsISO8601
import kotlin.time.Clock

/**
 * Represents a QR code badge assigned to a given person.
 *
 * @property personGuid the person guid
 * @property qrCodeUrl the URL of the assigned QR code badge for this person, or null if no badge is
 *           assigned to the given person.
 */
@Serializable
data class PersonQrBadge(
    val personGuid: String,
    val qrCodeUrl: Url?,
    override val lastModified: InstantAsISO8601,
    override val stored: InstantAsISO8601 = Clock.System.now(),
    val status: StatusEnum = StatusEnum.ACTIVE,
):  ModelWithTimes
