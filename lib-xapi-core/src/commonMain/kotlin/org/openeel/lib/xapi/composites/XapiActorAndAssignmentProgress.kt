package world.respect.lib.xapi.composites

import world.respect.lib.xapi.model.XapiActor

/**
 * Represents the progress of a given actor (eg. a student) on an assignment that follows the
 * ASSIGNMENT_RECIPE.
 *
 * @param actor the actor for which progress is being provided
 * @param progressPerTask a list of progress per assigned activityId.
 */
data class XapiActorAndAssignmentProgress(
    val actor: XapiActor,
    val progressPerTask: List<XapiAssignmentTaskProgress>,
)