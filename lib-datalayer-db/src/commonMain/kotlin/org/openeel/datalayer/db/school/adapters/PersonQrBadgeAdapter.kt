package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PersonQrBadgeEntity
import world.respect.datalayer.school.model.PersonQrBadge

fun PersonQrBadge.asEntity(
    uidNumberMapper: UidNumberMapper
): PersonQrBadgeEntity {
    return PersonQrBadgeEntity(
        pqrGuid = personGuid,
        pqrGuidNum =  uidNumberMapper(personGuid),
        pqrLastModified = lastModified,
        pqrStored = stored,
        pqrQrCodeUrl = qrCodeUrl,
        pqrStatus = status,
    )
}

fun PersonQrBadgeEntity.asModel(): PersonQrBadge {
    return PersonQrBadge(
        personGuid = pqrGuid,
        qrCodeUrl = pqrQrCodeUrl,
        lastModified = pqrLastModified,
        stored = pqrStored,
        status = pqrStatus,
    )
}
