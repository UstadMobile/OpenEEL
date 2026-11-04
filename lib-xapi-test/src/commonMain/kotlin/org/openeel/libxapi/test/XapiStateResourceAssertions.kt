package org.openeel.libxapi.test

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import org.openeel.lib.xapi.resources.XapiStateResource
import kotlin.time.Clock
import kotlin.uuid.Uuid

/**
 * Common test parameters and example documents for xAPI State resource tests.
 */
object XapiStateTestParams {
    const val ACTIVITY_ID1 = "http://example.com/activities/course-1"
    const val ACTIVITY_ID2 = "http://example.com/activities/course-2"
    val AGENT1 = XapiAgent(mbox = "mailto:user1@example.com")
    val AGENT2 = XapiAgent(mbox = "mailto:user2@example.com")
    val REGISTRATION1: Uuid? = null
    const val STATE_ID1 = "state-1"
    const val STATE_ID_NON_EXISTENT = "non-existent-state"

    val SINGLE_DOC_PARAMS1 = XapiStateResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        agent = AGENT1,
        registration = REGISTRATION1,
        stateId = STATE_ID1,
    )

    val SINGLE_DOC_PARAMS2 = XapiStateResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        agent = AGENT1,
        registration = REGISTRATION1,
        stateId = STATE_ID1,
    )

    val SINGLE_DOC_NON_EXISTENT_PARAMS = XapiStateResource.SingleDocumentParams(
        activityId = ACTIVITY_ID1,
        agent = AGENT1,
        registration = REGISTRATION1,
        stateId = STATE_ID_NON_EXISTENT,
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
