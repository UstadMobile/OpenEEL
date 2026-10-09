package world.respect.lib.xapi.model

import io.ktor.http.Url
import kotlinx.serialization.Serializable
import kotlin.time.Instant

/**
 * A summary of an assignment for list display.
 */
@Serializable
data class AssignmentSummary(
    val activityId: String,
    val title: String,
    val assignedActor: XapiActor,
    val lastModified: Instant,
    val deadline: Instant?,
    val completedCount: Int,
    val totalCount: Int,
    val learningUnitManifestUrls: List<Url> = emptyList(),
    val averageScore: Float? = null,
) {
    val isCompleted: Boolean get() = totalCount > 0 && completedCount == totalCount
}
