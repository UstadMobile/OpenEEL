package world.respect.datalayer.db.school.xapi.writequeue

import kotlinx.coroutines.flow.Flow
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.xapi.adapters.asEntity
import world.respect.datalayer.db.school.xapi.adapters.asModel
import world.respect.lib.xapi.remotewritequeue.EnqueueDrainXapiRemoteWriteQueueUseCase
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import world.respect.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import world.respect.libutil.util.time.systemTimeInMillis

class XapiRemoteWriteQueueDbImpl(
    private val schoolDb: RespectSchoolDatabase,
    private val account: AuthenticatedUserPrincipalId,
    private val enqueueDrainRemoteWriteQueueUseCase: EnqueueDrainXapiRemoteWriteQueueUseCase,
): XapiRemoteWriteQueue {

    override suspend fun add(items: List<XapiRemoteWriteQueueItem>) {
        schoolDb.getXapiRemoteWriteQueueItemEntityDao().upsert(
            items.map { it.asEntity(account.guid) }
        )
        enqueueDrainRemoteWriteQueueUseCase()
    }

    override suspend fun getPending(limit: Int): List<XapiRemoteWriteQueueItem> {
        return schoolDb.getXapiRemoteWriteQueueItemEntityDao().getPending(
            accountGuid = account.guid,
            limit = limit,
        ).map {
            it.asModel()
        }
    }

    override suspend fun markSent(ids: List<Int>) {
        schoolDb.getXapiRemoteWriteQueueItemEntityDao().updateTimeWritten(
            ids = ids,
            timeWritten = systemTimeInMillis(),
        )
    }

    override fun queueSizeAsFlow(): Flow<Int> {
        return schoolDb.getXapiRemoteWriteQueueItemEntityDao().pendingCountAsFlow(
            accountGuid = account.guid,
        )
    }

}