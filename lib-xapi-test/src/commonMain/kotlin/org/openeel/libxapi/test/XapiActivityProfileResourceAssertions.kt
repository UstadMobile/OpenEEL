package org.openeel.libxapi.test

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import kotlin.time.Clock

/**
 * Common test parameters and example documents for xAPI Activity Profile resource tests.
 */
object XapiActivityProfileTestParams {
    const val ACTIVITY_ID1 = "http://example.com/activities/course-1"
    const val ACTIVITY_ID2 = "http://example.com/activities/course-2"
    const val PROFILE_ID1 = "profile-1"
    const val PROFILE_ID_NON_EXISTENT = "non-existent-profile"

    val SINGLE_DOC_PARAMS1 = XapiActivityProfileResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        profileId = PROFILE_ID1,
    )

    val SINGLE_DOC_PARAMS2 = XapiActivityProfileResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        profileId = PROFILE_ID1,
    )


    val SINGLE_DOC_NON_EXISTENT_PARAMS = XapiActivityProfileResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        profileId = PROFILE_ID_NON_EXISTENT,
    )

    val DOC: XapiDocument = XapiDocumentByteArrayImpl(
        type = "application/json",
        updated = Clock.System.now().toGMTDate(),
        contents = """{"initialKey": "initialValue", "nested": {"a": 1}}""".encodeToByteArray(),
    )

    val DOC_JSON = buildJsonObject {
        put("initialKey", JsonPrimitive("initialValue"))
        put("nested", buildJsonObject {
            put("a", JsonPrimitive(1))
        })
    }

    val DOC_UPDATED: XapiDocument = XapiDocumentByteArrayImpl(
        type = "application/json",
        updated = Clock.System.now().toGMTDate(),
        contents = """{"newKey": "newValue"}""".encodeToByteArray(),
    )

    val DOC_UPDATED_JSON = buildJsonObject {
        put("newKey", JsonPrimitive("newValue"))
    }

    val DOC_NON_JSON: XapiDocument = XapiDocumentByteArrayImpl(
        type = "text/plain",
        updated = Clock.System.now().toGMTDate(),
        contents = "plain text content".encodeToByteArray(),
    )
}
