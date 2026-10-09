package org.openeel.datalayer.db.shared

import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.school.model.PullSyncStatus
import org.openeel.datalayer.shared.pullsync.PullSyncTracker

class PullSyncTrackerDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val uidNumberMapper: UidNumberMapper,
) : PullSyncTracker{

    override suspend fun getPullSyncStatus(tableId: Int): PullSyncStatus? {
        return schoolDb.getPullSyncStatusEntityDao().getStatus(
            personUidNum = uidNumberMapper(authenticatedUser.guid),
            tableId = tableId,
        )?.toModel()
    }

    override suspend fun updatePullSyncStatus(status: PullSyncStatus) {
        schoolDb.getPullSyncStatusEntityDao().upsert(
            listOf(status.toEntity(uidNumberMapper))
        )
    }

}