package org.openeel.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.ext.updateFromRemoteIfNeeded
import org.openeel.datalayer.ext.updateFromRemoteListIfNeeded
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import org.openeel.datalayer.repository.shared.paging.loadAndUpdateLocal2
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.ClassDataSourceLocal
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.writequeue.RemoteWriteQueue
import org.openeel.datalayer.school.writequeue.WriteQueueItem
import org.openeel.datalayer.shared.RepositoryModelDataSource
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.libutil.util.time.systemTimeInMillis

class ClassDataSourceRepository(
    override val local: ClassDataSourceLocal,
    override val remote: ClassDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : ClassDataSource, RepositoryModelDataSource<Clazz> {

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz> {
        local.updateFromRemoteIfNeeded(
            remote.findByGuid(params, guid), validationHelper
        )
        return local.findByGuid(params, guid)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): IPagingSourceFactory<Int, Clazz> {
        val remoteSource = remote.listAsPagingSource(loadParams, params).invoke()
        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(loadParams, params),
            onRemoteLoad = { remoteLoadParams ->
                remoteSource.loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal
                )
            },
            tag = { "ClassRepo.listAsPagingSource(params=$params)" }
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): DataLoadState<List<Clazz>> {
        local.updateFromRemoteListIfNeeded(
            remote.list(loadParams, params), validationHelper
        )
        return local.list(loadParams, params)
    }

    override suspend fun store(list: List<Clazz>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.CLASS,
                    uid = it.guid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}