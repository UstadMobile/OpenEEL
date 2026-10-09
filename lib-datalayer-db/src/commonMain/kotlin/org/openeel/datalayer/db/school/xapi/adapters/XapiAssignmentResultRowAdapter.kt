package org.openeel.datalayer.db.school.xapi.adapters

import org.openeel.datalayer.db.school.xapi.composites.XapiAssignmentResultRow
import org.openeel.lib.xapi.composites.XapiAssignmentTaskProgress

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