package org.openeel.datalayer.db.school.xapi.writequeue

import kotlinx.coroutines.flow.Flow
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.xapi.adapters.asEntity
import org.openeel.datalayer.db.school.xapi.adapters.asModel
import org.openeel.lib.xapi.remotewritequeue.EnqueueDrainXapiRemoteWriteQueueUseCase
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueue
import org.openeel.lib.xapi.remotewritequeue.XapiRemoteWriteQueueItem
import org.openeel.libutil.util.time.systemTimeInMillis

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