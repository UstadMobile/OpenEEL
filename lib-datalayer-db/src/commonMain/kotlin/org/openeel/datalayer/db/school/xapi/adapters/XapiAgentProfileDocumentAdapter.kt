package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentEntity
import world.respect.datalayer.db.shared.InstantAsTimestampString
import world.respect.lib.dataloadstate.datetime.toInstant
import world.respect.lib.xapi.ext.requireIfi
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiAgentProfileResource
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
