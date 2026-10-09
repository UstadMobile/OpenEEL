package world.respect.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import world.respect.datalayer.db.school.xapi.entities.XapiActivityProfileDocumentEntity

/**
 * The XapiActivityProfileDocumentEntity and its corresponding sha1 digest
 *
 * @param document [XapiActivityProfileDocumentEntity]
 * @param sha1 the SHA-1 for the document param
 */
class XapiActivityProfileDocumentAndShaEntities(
    @Embedded
    val document: XapiActivityProfileDocumentEntity,
    val sha1: String
)
