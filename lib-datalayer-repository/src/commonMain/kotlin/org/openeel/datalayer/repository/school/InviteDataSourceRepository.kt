package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.InviteDataSourceLocal
import world.respect.datalayer.school.model.Invite2
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory

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
