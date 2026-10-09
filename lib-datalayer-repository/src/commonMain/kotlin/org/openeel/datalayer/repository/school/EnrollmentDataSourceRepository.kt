package world.respect.datalayer.repository.school

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.lib.dataloadstate.ext.combineWithRemoteIfNotNull
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.ext.updateFromRemoteListIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.DataLayerTags
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.libutil.util.time.systemTimeInMillis

class EnrollmentDataSourceRepository(
    override val local: EnrollmentDataSourceLocal,
    override val remote: EnrollmentDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : EnrollmentDataSource, RepositoryModelDataSource<Enrollment> {

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Enrollment> {
        local.updateFromRemoteIfNeeded(
            remoteLoad = remote.findByGuid(loadParams, guid),
            validationHelper = validationHelper,
        )

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<Enrollment>> {
        return local.findByGuidAsFlow(loadParams, guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(loadParams, guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Enrollment> {
        val remote = remote.listAsPagingSource(
            loadParams = loadParams,
            listParams = listParams.copy(common = listParams.common.copy(includeDeleted = true))
        ).invoke()
        return RepositoryPagingSourceFactory(
            local = local.listAsPagingSource(loadParams, listParams),
            onRemoteLoad = { remoteLoadParams ->
                remote.loadAndUpdateLocal2(
                    loadParams = remoteLoadParams,
                    onUpdateLocalFromRemote = local::updateLocal,
                )
            },
            tag = { "EnrollmentDataSourceRepo(listParams=$listParams)" }
        )
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): DataLoadState<List<Enrollment>> {
        val remote = try {
            remote.list(loadParams, listParams).also {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        }catch(e: Throwable) {
            Napier.w(
                message = "EnrollmentDataSourceRepository.list() failed:",
                throwable = e,
                tag = DataLayerTags.TAG_DATALAYER
            )
            null
        }

        return local.list(loadParams, listParams).combineWithRemoteIfNotNull(remote)
    }

    override suspend fun store(list: List<Enrollment>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.ENROLLMENT,
                    uid = it.uid,
                    timeQueued = timeNow,
                )
            }
        )
    }

}
