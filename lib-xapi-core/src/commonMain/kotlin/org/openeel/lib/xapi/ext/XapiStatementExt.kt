package world.respect.lib.xapi.ext

import io.ktor.http.toHttpDate
import io.ktor.util.date.GMTDate
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import kotlin.time.Instant

/**
 * When an xAPI agent is used in an actor field, objectType is not required. When it is the object
 * of a statement, the objectType is required.
 *
 * XapiGroup and XapiStatementRef always serialize the objectType. If the object is an Activity, then
 * objectType can be omitted.
 */
@Suppress("IntroduceWhenSubject")
fun XapiStatement.addObjectTypeIfRequired(): XapiStatement {
    val stmtObject = `object`

    return when {
        stmtObject is XapiAgent && stmtObject.objectType == null -> this.copy(
            `object` = stmtObject.copy(objectType = XapiObjectType.Agent)
        )
        stmtObject is XapiStatement && stmtObject.objectType == null -> this.copy(
            `object` = stmtObject.copy(objectType = XapiObjectType.Statement)
        )
        else -> this
    }
}


/**
 * Add the assignmentActivityId to the statement as per the Assignment Recipe, if not already present.
 * This is used when a statement is received (from an activity provider e.g. a lesson) to link the
 * statement with the parent. if not already present.
 *
 * @param assignmentActivityId the activity id of the assignment
 * @return the updated statement
 */
fun XapiStatement.asAssignmentRecipeStmt(
    assignmentActivityId: String
): XapiStatement {

    val idInGrouping = context?.contextActivities?.grouping?.any {
        it.id == assignmentActivityId
    } == true

    return if(!idInGrouping) {
        this.copy(
            context = (context ?: XapiContext()).copy(
                contextActivities = (context?.contextActivities ?: XapiContextActivities()).copy(
                    grouping = buildList {
                        context?.contextActivities?.grouping?.also {
                            addAll(it)
                        }
                        add(XapiActivity(id = assignmentActivityId))
                    }
                )
            )
        )
    }else {
        this
    }
}

fun XapiStatement.asAssignmentRecipeStmtIfIdNotNull(
    assignmentActivityId: String?
): XapiStatement {
    return assignmentActivityId?.let {
        this.asAssignmentRecipeStmt(it)
    } ?: this
}

/**
 * Get the last-modified for a list of statements being returned as per
 *
 * https://github.com/adlnet/xAPI-Spec/blob/master/xAPI-Communication.md#213-get-statements
 */
fun List<XapiStatement>.lastModifiedForRetrievedStatements(
    ascendingOrder: Boolean = true,
    consistentThrough: Instant,
): Instant {
    return if(ascendingOrder && isNotEmpty()) {
        last().stored ?: throw IllegalArgumentException("Retrieved statement should have stored set")
    }else if(!ascendingOrder && isNotEmpty()) {
        first().stored ?: throw IllegalArgumentException("Retrieved statement should have stored set")
    }else {
        consistentThrough
    }
}

fun List<XapiStatement>.lastModifiedGMTStringForRetrievedStatements(
    ascendingOrder: Boolean,
    consistentThrough: Instant,
) : String {
    return GMTDate(
        lastModifiedForRetrievedStatements(
            ascendingOrder, consistentThrough
        ).toEpochMilliseconds()
    ).toHttpDate()
}

fun List<XapiStatement>.mostRecentByTimestampOrNull() : XapiStatement? {
    return maxByOrNull { it.timestamp ?: EPOCH }
}

fun List<XapiStatement>.mostRecentByTimestamp(): XapiStatement {
    return mostRecentByTimestampOrNull() ?: throw IllegalArgumentException("No statements in list")
}

fun XapiStatement.contextOrBlank(): XapiContext {
    return context ?: XapiContext()
}

/**
 * Add the activityToAdd to the contextActivities.grouping list if not already found
 */
fun XapiStatement.addActivityToContextActivitiesGrouping(
    activityToAdd: XapiActivity
): XapiStatement {
    return copy(
        context = contextOrBlank().let { ctx ->
            ctx.copy(
                contextActivities = ctx.contextActivitiesOrBlank().let { ctxActivities ->
                    ctxActivities.copy(
                        grouping = (ctxActivities.grouping ?: emptyList()).addOrReplaceById(
                            other = activityToAdd
                        )
                    )
                }
            )
        }
    )
}

fun XapiStatement.addActivitiesToContextActivitiesGrouping(
    activitiesToAdd: List<XapiActivity>
): XapiStatement {
    return activitiesToAdd.fold(this) { stmt, activity ->
        stmt.addActivityToContextActivitiesGrouping(activity)
    }
}


fun XapiStatement.removeActivityFromContextActivitiesGrouping(
    idToRemove: String
) : XapiStatement {
    return copy(
        context = contextOrBlank().let { ctx ->
            ctx.copy(
                contextActivities = ctx.contextActivitiesOrBlank().let { ctxActivities ->
                    ctxActivities.copy(
                        grouping = (ctxActivities.grouping ?: emptyList()).filter {
                            it.id != idToRemove
                        }
                    )
                }
            )
        }
    )
}

fun XapiStatement.objectActivityOrNull(): XapiActivity? {
    return `object` as? XapiActivity
}

fun XapiStatement.objectActivityNameOrNull(): Map<String, String>? {
    return objectActivityOrNull()?.definition?.name
}

fun XapiStatement.objectSubstatementOrNull(): XapiStatement? {
    return `object` as? XapiStatement
}

/**
 * Given a list of statements, filter to include only the most recent statement per object id
 * (e.g. per activity id etc).
 */
fun List<XapiStatement>.distinctByMostRecentTimestampForActivityId(): List<XapiStatement> {
    return mapNotNull {
        it.`object`.idAsStringOrNull()?.let { id -> Pair(id, it) }
    }.groupBy {
        it.first
    }.map { entry ->
        entry.value.maxBy { it.second.timestamp ?: EPOCH }.second
    }
}

fun List<XapiStatement>.sortedByTimestampDescending() : List<XapiStatement> {
    return sortedByDescending { it.timestamp ?: EPOCH }
}

/**
 * Returns a copy of the statement with the activity name updated. If the object is not an activity,
 * it has no effect
 */
fun XapiStatement.copyWithObjectActivityName(
    name: Map<String, String>
) : XapiStatement {
    return copy(
        `object` = objectActivityOrNull()?.copyWithDefinitionName(name) ?: this.`object`
    )
}

fun XapiStatement.copyWithObjectActivityDescription(
    description: Map<String, String>
): XapiStatement {
    return copy(
        `object` = objectActivityOrNull()?.copyWithDefinitionDescription(description) ?: this.`object`
    )
}