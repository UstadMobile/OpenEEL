package org.openeel.datalayer.db.school.adapters

import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.entities.PersonQrBadgeEntity
import org.openeel.datalayer.school.model.PersonQrBadge

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
