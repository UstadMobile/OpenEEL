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
import world.respect.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import world.respect.datalayer.school.PersonQrBadgeDataSource
import world.respect.datalayer.school.model.PersonQrBadge
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.DataLayerTags
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.libutil.util.time.systemTimeInMillis

class PersonQrCodeBadgeDataSourceRepository(
    override val local: PersonQrCodeBadgeDataSourceLocal,
    override val remote: PersonQrBadgeDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : PersonQrBadgeDataSource, RepositoryModelDataSource<PersonQrBadge> {

    override suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: PersonQrBadgeDataSource.GetListParams
    ): DataLoadState<List<PersonQrBadge>> {
        val remote = try {
            remote.listAll(loadParams, listParams).also {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        } catch (e: Throwable) {
            Napier.w(
                message = "PersonQrCodeDataSourceRepository.list() failed:",
                throwable = e,
                tag = DataLayerTags.TAG_DATALAYER
            )
            null
        }

        return local.listAll(loadParams, listParams).combineWithRemoteIfNotNull(remote)
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrBadgeDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonQrBadge>>> {
        return local.listAllAsFlow(loadParams, listParams).combineWithRemote(
            remoteFlow = remote.listAllAsFlow(
                loadParams,
                listParams.copy(common = listParams.common.copy(includeDeleted = true))
            ).onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonQrBadge>> {
        return local.findByGuidAsFlow(loadParams, guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(loadParams, guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun store(list: List<PersonQrBadge>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON_QRBADGE,
                    uid = it.personGuid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}