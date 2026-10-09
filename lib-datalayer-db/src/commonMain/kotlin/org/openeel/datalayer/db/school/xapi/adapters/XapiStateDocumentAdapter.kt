package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.db.school.xapi.entities.XapiStateDocumentEntity
import world.respect.datalayer.db.shared.InstantAsTimestampString
import world.respect.lib.dataloadstate.datetime.toInstant
import world.respect.lib.xapi.ext.requireIfi
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiStateResource
import kotlin.uuid.Uuid

/**
 * Converts an [XapiDocument] and [XapiStateResource.SingleDocumentParams]
 * to an [XapiStateDocumentEntity].
 */
suspend fun XapiDocument.toXapiStateDocumentEntity(
    params: XapiStateResource.SingleDocumentParams,
    id: String? = null,
): XapiStateDocumentEntity {
    return XapiStateDocumentEntity(
        id = id ?: Uuid.random().toString(),
        stateId = params.stateId,
        activityIri = params.activityId,
        agentIfi = params.agent.requireIfi(),
        registration = params.registration,
        contentType = this.type,
        contents = this.contentsAsByteArray(),
        lastModified = InstantAsTimestampString(updated.toInstant()),
    )
}

fun XapiStateDocumentEntity.toModel(): XapiDocument = this
