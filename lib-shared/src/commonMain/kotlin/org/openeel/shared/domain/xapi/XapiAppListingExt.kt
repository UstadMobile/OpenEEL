package world.respect.shared.domain.xapi

import kotlinx.serialization.json.JsonPrimitive
import world.respect.lib.xapi.OpenEelXapiConstants
import world.respect.lib.xapi.model.XapiActivity
import world.respect.lib.xapi.model.XapiActivityDefinition
import world.respect.lib.xapi.model.XapiActor
import world.respect.lib.xapi.model.XapiContext
import world.respect.lib.xapi.model.XapiContextActivities
import world.respect.lib.xapi.model.XapiObjectType
import world.respect.lib.xapi.model.XapiStatement
import world.respect.lib.xapi.model.XapiVerb
import kotlin.time.Clock
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

object XapiAppListingConstants {
    const val ACTIVITY_TYPE_APPLICATION = "http://activitystrea.ms/schema/1.0/application"
}

@OptIn(ExperimentalUuidApi::class)
fun createBlankAppListingStatement(
    appActivityId: String,
    appTitle: Map<String, String>,
    actor: XapiActor,
    description: Map<String, String>?= null,
    moreInfo: String? = null,
    manifestUrl: String = appActivityId
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(id = XapiVerb.ID_LISTED_APP),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = appActivityId,
            definition = XapiActivityDefinition(
                name = appTitle,
                description = description,
                type = XapiAppListingConstants.ACTIVITY_TYPE_APPLICATION,
                moreInfo = moreInfo,
                extensions = mapOf(
                    OpenEelXapiConstants.ACTIVITY_EXTENSION_WEBPUB_MANIFEST_LINK to JsonPrimitive(manifestUrl)
                )
            )
        ),
        context = XapiContext(
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = OpenEelXapiConstants.CATEGORY_APP_LISTING_RECIPE,
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