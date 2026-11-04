package org.openeel.datalayer.db.school.xapi.adapters

import org.openeel.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import org.openeel.datalayer.db.shared.InstantAsTimestampString
import org.openeel.lib.dataloadstate.datetime.toInstant
import org.openeel.lib.xapi.model.XapiDocument
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import kotlin.uuid.Uuid

/**
 * Converts an [XapiDocument] and [XapiActivityProfileResource.SingleDocumentParams]
 * to an [XapiActivityProfileDocumentEntity].
 */
suspend fun XapiDocument.toXapiActivityProfileDocumentEntity(
    params: XapiActivityProfileResource.SingleDocumentParams,
    id: String? = null,
): XapiActivityProfileDocumentEntity {
    return XapiActivityProfileDocumentEntity(
        id = id ?: Uuid.random().toString(),
        profileId = params.profileId,
        activityIri = params.activityId,
        contentType = this.type,
        contents = this.contentsAsByteArray(),
        lastModified = InstantAsTimestampString(updated.toInstant()),
    )
}

fun XapiActivityProfileDocumentEntity.toModel(): XapiDocument = this
