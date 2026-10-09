package world.respect.datalayer.db.school.xapi.composites

import androidx.room.Embedded
import world.respect.datalayer.db.school.xapi.entities.XapiStateDocumentEntity

/**
 * The XapiStateDocumentEntity and its corresponding sha1 digest
 *
 * @param document [XapiStateDocumentEntity]
 * @param sha1 the SHA-1 for the document param
 */
class XapiStateDocumentAndShaEntities(
    @Embedded
    val document: XapiStateDocumentEntity,
    val sha1: String
)
