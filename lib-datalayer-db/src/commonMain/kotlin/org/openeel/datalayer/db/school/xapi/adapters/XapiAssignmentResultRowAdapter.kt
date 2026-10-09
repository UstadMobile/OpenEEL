package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.db.school.xapi.composites.XapiAssignmentResultRow
import world.respect.lib.xapi.composites.XapiAssignmentTaskProgress

fun XapiAssignmentResultRow.toXapiAssignmentResult(
    activityId: String,
): XapiAssignmentTaskProgress {
    return XapiAssignmentTaskProgress(
        activityId = activityId,
        completed = verbCompleted ?: resultCompleted,
        successful = successful,
        scoreScaled = scoreScaled,
        progress = progress,
    )
}