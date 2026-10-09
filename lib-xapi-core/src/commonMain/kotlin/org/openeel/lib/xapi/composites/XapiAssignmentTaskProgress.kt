package world.respect.lib.xapi.composites


/**
 * Represents the progress on a particular assigned activityId for one actor. This is used as a
 * member variable of XapiActorAndAssignmentProgress, hence the actor is not a property here.
 *
 * The completed and successful fields will be:
 *  True if there is a matching statement with the result.field set to true
 *  False if there is no matching statement with the result.field set to true, but there is a
 *  matching statement with the result set to false.
 *  Null if there is no matching statement with the result set to true or false.
 *
 *
 * @param activityId the activityId this represents progress for
 * @param completed set as above
 * @param successful set as above
 * @param scoreScaled the highest raw score for this activityId, null if there are no matching
 *        statements with a non-null score.
 * @param progress the maximum progress extension value on any matching statement, null if there is
 *        none.
 *
 */
data class XapiAssignmentTaskProgress(
    val activityId: String,
    val completed: Boolean? = null,
    val successful: Boolean? = null,
    val scoreScaled: Float? = null,
    val progress: Int? = null,
) {

    companion object {

        fun emptyResult(activityId: String): XapiAssignmentTaskProgress {
            return XapiAssignmentTaskProgress(
                activityId = activityId,
                completed = null,
                successful = null,
                scoreScaled = null,
                progress = null,
            )
        }

    }
}
