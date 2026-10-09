package world.respect.datalayer.db.school.xapi.adapters

import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity
import world.respect.datalayer.db.shared.InstantAsTimestampString
import world.respect.lib.dataloadstate.datetime.toInstant
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiActivityProfileResource
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
