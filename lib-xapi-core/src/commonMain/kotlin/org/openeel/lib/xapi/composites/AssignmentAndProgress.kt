package world.respect.lib.xapi.composites

import world.respect.lib.xapi.model.XapiStatement

/**
 * @param assignmentStatement the Statement that set the assignment as per the assignment recipe
 * @param progress a list of progress per actor (eg. student)
 */
data class AssignmentAndProgress(
    val assignmentStatement: XapiStatement?,
    val progress: List<XapiActorAndAssignmentProgress>,
)
