package world.respect.datalayer.db.school.writequeue

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.school.writequeue.EnqueueDrainRemoteWriteQueueUseCase
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.libutil.util.time.systemTimeInMillis

class RemoteWriteQueueDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val account: AuthenticatedUserPrincipalId,
    private val enqueueDrainRemoteWriteQueueUseCase: EnqueueDrainRemoteWriteQueueUseCase,
): RemoteWriteQueue {

    override suspend fun add(items: List<WriteQueueItem>) {
        schoolDb.getWriteQueueItemEntityDao().upsert(
            items.map { it.asEntity(account.guid) }
        )
        enqueueDrainRemoteWriteQueueUseCase()
    }

    override suspend fun getPending(limit: Int): List<WriteQueueItem> {
        return schoolDb.getWriteQueueItemEntityDao().getPending(
            accountGuid = account.guid,
            limit = limit,
        ).map {
            it.asModel()
        }
    }

    override suspend fun markSent(ids: List<Int>) {
        schoolDb.getWriteQueueItemEntityDao().updateTimeWritten(
            ids = ids,
            timeWritten = systemTimeInMillis(),
        )
    }

    override fun queueSizeAsFlow(): Flow<Int> {
        return schoolDb.getWriteQueueItemEntityDao().pendingCountAsFlow(
            accountGuid = account.guid,
        )
    }

}