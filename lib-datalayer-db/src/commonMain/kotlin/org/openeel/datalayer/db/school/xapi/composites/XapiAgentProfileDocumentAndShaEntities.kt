package world.respect.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import world.respect.datalayer.db.school.xapi.entities.XapiAgentProfileDocumentEntity

/**
 * The XapiAgentProfileDocumentEntity and its corresponding sha1 digest
 *
 * @param document [XapiAgentProfileDocumentEntity]
 * @param sha1 the SHA-1 for the document param
 */
class XapiAgentProfileDocumentAndShaEntities(
    @Embedded
    val document: XapiAgentProfileDocumentEntity,
    val sha1: String
)
