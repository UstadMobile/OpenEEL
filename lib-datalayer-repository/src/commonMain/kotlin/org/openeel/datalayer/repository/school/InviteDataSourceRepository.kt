package org.openeel.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.ext.updateFromRemoteIfNeeded
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import org.openeel.datalayer.repository.shared.paging.loadAndUpdateLocal2
import org.openeel.datalayer.school.InviteDataSource
import org.openeel.datalayer.school.InviteDataSourceLocal
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.writequeue.RemoteWriteQueue
import org.openeel.datalayer.school.writequeue.WriteQueueItem
import org.openeel.datalayer.shared.RepositoryModelDataSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory

class InviteDataSourceRepository(
    override val local: InviteDataSourceLocal,
    override val remote: InviteDataSource,
    private val remoteWriteQueue: RemoteWriteQueue,
    private val validationHelper: ExtendedDataSourceValidationHelper
) : InviteDataSource, RepositoryModelDataSource<Invite2> {

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: InviteDataSource.GetListParams
    ): IPagingSourceFactory<Int, Invite2> {
        val remoteSource = remote.listAsPagingSource(loadParams, params).invoke()
        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(loadParams, params),
            onRemoteLoad = { remoteLoadParams ->
                remoteSource.loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal
                )
            },
            tag = { "invite.listAsPagingSource(params=$params)" }
        )
    }

    override suspend fun findByGuid(guid: String): DataLoadState<Invite2> {
        local.updateFromRemoteIfNeeded(
            remote.findByGuid(guid), validationHelper
        )
        return local.findByGuid(guid)
    }

    override fun findByUidAsFlow(
        uid: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<Invite2>> {
        return local.findByUidAsFlow(uid = uid, loadParams = loadParams).combineWithRemote(
            remoteFlow = remote.findByUidAsFlow(uid = uid, loadParams = loadParams).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun findByCode(code: String): DataLoadState<Invite2> {
        local.updateFromRemoteIfNeeded(
            remote.findByCode(code), validationHelper
        )
        return local.findByCode(code)    }


    override suspend fun store(list: List<Invite2>) {
        local.store(list)
        val timeNow = System.currentTimeMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.INVITE,
                    uid = it.uid,
                    timeQueued = timeNow
                )
            }
        )
    }
}
