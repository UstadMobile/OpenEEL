package world.respect.shared.domain.xapi

import kotlinx.serialization.json.JsonPrimitive
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiGroup
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
object XapiAssignmentConstants {
    const val EXT_CREATED = "https://id.ustadmobile.com/xapi/extension/created"
}


@OptIn(ExperimentalUuidApi::class)
fun XapiStatement.withDeadline(deadline: Instant?): XapiStatement {
    val activity = `object` as? XapiActivity ?: return this
    val definition = activity.definition ?: XapiActivityDefinition()
    val newExtensions = (definition.extensions ?: emptyMap()).toMutableMap().apply {
        if (deadline != null) {
            put(OpenEelXapiConstants.ACTIVITY_EXTENSION_DEADLINE, JsonPrimitive(deadline.toString()))
        } else {
            remove(OpenEelXapiConstants.ACTIVITY_EXTENSION_DEADLINE)
        }
    }
    return copy(
        `object` = activity.copy(
            definition = definition.copy(extensions = newExtensions)
        )
    )
}


@OptIn(ExperimentalUuidApi::class)
fun createBlankAssignmentStatement(
    assignmentActivityId: String,
    instructor: XapiActor
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = XapiGroup(name = "", objectType = XapiObjectType.Group),
        verb = XapiVerb(
            id = XapiVerb.ID_ASSIGN,
        ),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = assignmentActivityId,
            definition = XapiActivityDefinition(
                type = XapiActivityDefinition.TYPE_ASSIGNMENT,
                extensions = mapOf(XapiAssignmentConstants.EXT_CREATED to JsonPrimitive(now.toString()))
            )
        ),
        context = XapiContext(
            instructor = instructor,
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = OpenEelXapiConstants.CATEGORY_ASSIGNMENT_RECIPE,
                        objectType = XapiObjectType.Activity
                    )
                ),
                grouping = emptyList()
            )
        ),
        timestamp = now,
        version = "1.0.0"
    )
}
