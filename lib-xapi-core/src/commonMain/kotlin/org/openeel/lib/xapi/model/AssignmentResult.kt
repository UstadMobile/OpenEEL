package world.respect.lib.xapi.model

import kotlinx.serialization.Serializable

@Serializable
data class AssignmentResult(
    val personUid: String,
    val personName: String?,
    val activityId: String,
    val completion: Boolean?,
    val success: Boolean?,
    val scoreScaled: Float?,
    val progress: Int?
)
