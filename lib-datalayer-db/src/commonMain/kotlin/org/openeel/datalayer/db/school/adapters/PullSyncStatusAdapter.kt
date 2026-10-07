package org.openeel.datalayer.db.school.adapters

import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.school.entities.PullSyncStatusEntity
import org.openeel.datalayer.school.model.PullSyncStatus

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

