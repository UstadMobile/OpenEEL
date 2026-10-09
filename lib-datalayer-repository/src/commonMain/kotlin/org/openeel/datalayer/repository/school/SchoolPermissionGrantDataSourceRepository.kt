package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.SchoolPermissionGrantDataSource
import world.respect.datalayer.school.SchoolPermissionGrantDataSourceLocal
import world.respect.datalayer.school.model.SchoolPermissionGrant
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.libutil.util.time.systemTimeInMillis

class SchoolPermissionGrantDataSourceRepository(
    override val local: SchoolPermissionGrantDataSourceLocal,
    override val remote: SchoolPermissionGrantDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : SchoolPermissionGrantDataSource, RepositoryModelDataSource<SchoolPermissionGrant> {

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<SchoolPermissionGrant>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolPermissionGrant> {
        local.updateFromRemoteIfNeeded(
            remote.findByGuid(params, guid), validationHelper
        )
        return local.findByGuid(params, guid)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolPermissionGrantDataSource.GetListParams
    ): IPagingSourceFactory<Int, SchoolPermissionGrant> {
        val remoteSource = remote.listAsPagingSource(loadParams, params).invoke()
        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(loadParams, params),
            onRemoteLoad = { remoteLoadParams ->
                remoteSource.loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal
                )
            },
            tag = { "SchoolPermissionGrantRepo.listAsPagingSource" }
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolPermissionGrantDataSource.GetListParams
    ): DataLoadState<List<SchoolPermissionGrant>> {
        local.takeIf { !loadParams.onlyIfCached }?.updateFromRemoteListIfNeeded(
            remote.list(loadParams, params), validationHelper
        )
        return local.list(loadParams, params)
    }

    override suspend fun store(list: List<SchoolPermissionGrant>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.SCHOOL_PERMISSION_GRANT,
                    uid = it.uid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}
