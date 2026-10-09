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

/**
 * Creates an xAPI statement to pin an OPDS collection to the collections listing, as per the
 * Collections Listing recipe: README_COLLECTIONS_LISTING_RECIPE.md
 *
 * @param collectionActivityId the activity id of the OPDS collection itself i.e. the activityId
 *        used with the xAPI Activity Profile Resource (see
 *        README_OPDS_COLLECTIONS_ACTIVITY_PROFILE.md)
 * @param collectionName the name of the collection (language code to name)
 * @param actor the person pinning the collection
 * @param collectionDescription the description/subtitle of the collection (language code to
 *        description)
 * @param opdsCollectionLink the url that can be used to GET/PUT the OPDS collection via the xAPI
 *        Activity Profile Resource
 */
@OptIn(ExperimentalUuidApi::class)
fun createPinCollectionStatement(
    collectionActivityId: String,
    collectionName: Map<String, String>,
    actor: XapiActor,
    collectionDescription: Map<String, String>? = null,
    opdsCollectionLink: String,
): XapiStatement {
    val now = Clock.System.now()
    return XapiStatement(
        id = Uuid.random(),
        actor = actor,
        verb = XapiVerb(id = XapiVerb.ID_PIN_COLLECTION),
        `object` = XapiActivity(
            objectType = XapiObjectType.Activity,
            id = collectionActivityId,
            definition = XapiActivityDefinition(
                name = collectionName,
                description = collectionDescription,
                type = XapiAppListingConstants.ACTIVITY_TYPE_APPLICATION,
                extensions = mapOf(
                    OpenEelXapiConstants.ACTIVITY_EXTENSION_OPDS_COLLECTION_LINK to
                        JsonPrimitive(opdsCollectionLink)
                )
            )
        ),
        context = XapiContext(
            contextActivities = XapiContextActivities(
                category = listOf(
                    XapiActivity(
                        id = OpenEelXapiConstants.CATEGORY_COLLECTION_LISTING_RECIPE,
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
