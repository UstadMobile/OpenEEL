package world.respect.datalayer.school.model

import kotlinx.serialization.Serializable
import world.respect.lib.serializers.InstantAsISO8601

/**
 * @param accountPersonUid the personUid for which this PullSyncStatus is valid
 * @param consistentThrough the most recent consistent-through header used to set the since
 *        parameter for subsequent requests.
 * @param permissionsLastModified the most recent permissions-last-modified header used to set the
 *        sinceIfPermissionsNotChangedSince for subsequent requests.
 */
@Serializable
data class PullSyncStatus(
    val accountPersonUid: String,
    val consistentThrough: InstantAsISO8601,
    val permissionsLastModified: InstantAsISO8601,
    val tableId: Int,
)
