package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.SchoolPermissionGrantEntity
import world.respect.datalayer.school.model.SchoolPermissionGrant

fun SchoolPermissionGrantEntity.toModel(): SchoolPermissionGrant {
    return SchoolPermissionGrant(
        uid = spgUid,
        statusEnum = spgStatusEnum,
        toRole = spgToRole,
        permissions = spgPermissions,
        lastModified = spgLastModified,
        stored = spgStored,
    )
}

fun SchoolPermissionGrant.toEntity(uidNumberMapper: UidNumberMapper): SchoolPermissionGrantEntity {
    return SchoolPermissionGrantEntity(
        spgUid = uid,
        spgUidNum = uidNumberMapper(uid),
        spgStatusEnum = statusEnum,
        spgToRole = toRole,
        spgPermissions = permissions,
        spgLastModified = lastModified,
        spgStored = stored,
    )
}
