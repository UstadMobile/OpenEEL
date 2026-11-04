package org.openeel.libxapi.test

import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import org.openeel.lib.dataloadstate.datetime.toGMTDate
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.model.XapiDocumentByteArrayImpl
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import kotlin.time.Clock

/**
 * Common test parameters and example documents for xAPI Agent Profile resource tests.
 */
object XapiAgentProfileTestParams {
    val AGENT1 = XapiAgent(mbox = "mailto:user1@example.com")
    val AGENT2 = XapiAgent(mbox = "mailto:user2@example.com")
    const val PROFILE_ID1 = "profile-1"
    const val PROFILE_ID_NON_EXISTENT = "non-existent-profile"

    val SINGLE_DOC_PARAMS1 = XapiAgentProfileResource.SingleDocumentParams(
        agent = AGENT1,
        profileId = PROFILE_ID1,
    )

    val SINGLE_DOC_PARAMS2 = XapiAgentProfileResource.SingleDocumentParams(
        agent = AGENT1,
        profileId = PROFILE_ID1,
    )

    val SINGLE_DOC_NON_EXISTENT_PARAMS = XapiAgentProfileResource.SingleDocumentParams(
        agent = AGENT1,
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
