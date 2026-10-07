package org.openeel.datalayer.db.school.xapi.adapters

import org.openeel.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentEntity
import org.openeel.datalayer.db.shared.InstantAsTimestampString
import org.openeel.lib.dataloadstate.datetime.toInstant
import org.openeel.lib.xapi.ext.requireIfi
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import kotlin.uuid.Uuid

/**
 * Converts an [XapiDocument] and [XapiAgentProfileResource.SingleDocumentParams]
 * to an [XapiAgentProfileDocumentEntity].
 */
suspend fun XapiDocument.toXapiAgentProfileDocumentEntity(
    params: XapiAgentProfileResource.SingleDocumentParams,
    id: String? = null,
): XapiAgentProfileDocumentEntity {
    val bytes = this.contentsAsByteArray()
    return XapiAgentProfileDocumentEntity(
        id = id ?: Uuid.random().toString(),
        profileId = params.profileId,
        agentIfi = params.agent.requireIfi(),
        contentType = this.type,
        contents = bytes,
        contentLength = bytes.size,
        lastModified = InstantAsTimestampString(updated.toInstant()),
    )
}

fun XapiAgentProfileDocumentEntity.toModel(): XapiDocument = this
