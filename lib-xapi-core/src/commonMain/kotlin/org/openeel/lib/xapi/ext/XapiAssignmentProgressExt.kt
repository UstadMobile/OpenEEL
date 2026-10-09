package world.respect.lib.xapi.ext

import world.respect.lib.xapi.composites.XapiActorAndAssignmentProgress
import world.respect.lib.xapi.composites.XapiAssignmentTaskProgress

fun List<XapiAssignmentTaskProgress>.averageScore(): XapiAssignmentTaskProgress {
    return XapiAssignmentTaskProgress(
        activityId = "",
        scoreScaled = mapNotNull { it.scoreScaled }.takeIf { it.isNotEmpty() }?.average()?.toFloat()
    )
}

fun XapiAssignmentTaskProgress.isCompleted(): Boolean {
    return completed == true
}

fun XapiAssignmentTaskProgress.isInProgress(): Boolean {
    return !isCompleted() && (progress ?: 0) > 0
}

fun XapiAssignmentTaskProgress.isNotStarted(): Boolean {
    return !isCompleted() && (progress ?: 0) == 0
}

/**
 * Calculates the display percentage for an assignment progress unit.
 */
fun XapiAssignmentTaskProgress.calculatePercentage(): Int? {
    return progress ?: scoreScaled?.let { (it * 100).toInt() }
}

val XapiActorAndAssignmentProgress.personUid: String
    get() = actor.account?.name ?: ""

val XapiActorAndAssignmentProgress.isStarted: Boolean
    get() = progressPerTask.any { it.completed == true || (it.progress ?: 0) > 0 }

fun XapiActorAndAssignmentProgress.isCompleted(): Boolean {
    return progressPerTask.all { it.completed == true }
}
