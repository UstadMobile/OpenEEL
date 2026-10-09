package org.openeel.datalayer.db.school.xapi.adapters

import org.openeel.datalayer.db.school.xapi.entities.XapiStateDocumentEntity
import org.openeel.datalayer.db.shared.InstantAsTimestampString
import org.openeel.lib.dataloadstate.datetime.toInstant
import org.openeel.lib.xapi.ext.requireIfi
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.XapiStateResource
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
