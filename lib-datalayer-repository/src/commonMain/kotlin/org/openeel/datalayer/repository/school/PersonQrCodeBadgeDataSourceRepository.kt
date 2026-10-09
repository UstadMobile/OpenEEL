package org.openeel.datalayer.repository.school

import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.lib.dataloadstate.ext.combineWithRemoteIfNotNull
import org.openeel.datalayer.ext.updateFromRemoteIfNeeded
import org.openeel.datalayer.ext.updateFromRemoteListIfNeeded
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.datalayer.school.model.PersonQrBadge
import org.openeel.datalayer.school.writequeue.RemoteWriteQueue
import org.openeel.datalayer.school.writequeue.WriteQueueItem
import org.openeel.datalayer.shared.DataLayerTags
import org.openeel.datalayer.shared.RepositoryModelDataSource
import org.openeel.libutil.util.time.systemTimeInMillis

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