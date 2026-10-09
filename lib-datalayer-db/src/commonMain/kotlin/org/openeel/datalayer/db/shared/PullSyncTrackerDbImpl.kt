package world.respect.datalayer.db.shared

import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.model.PullSyncStatus
import world.respect.datalayer.shared.pullsync.PullSyncTracker

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