package world.respect.datalayer.db.school.adapters

import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.entities.PullSyncStatusEntity
import world.respect.datalayer.school.model.PullSyncStatus

fun PullSyncStatus.toEntity(
    uidNumberMapper: UidNumberMapper
): PullSyncStatusEntity {
    return PullSyncStatusEntity(
        pssAccountPersonUid = accountPersonUid,
        pssAccountPersonUidNum = uidNumberMapper(accountPersonUid),
        pssLastConsistentThrough = consistentThrough,
        pssTableId = tableId,
        pssPermissionsLastModified = permissionsLastModified,
    )
}

fun PullSyncStatusEntity.toModel(): PullSyncStatus {
    return PullSyncStatus(
        accountPersonUid = pssAccountPersonUid,
        consistentThrough = pssLastConsistentThrough,
        tableId = pssTableId,
        permissionsLastModified = pssPermissionsLastModified,
    )
}

