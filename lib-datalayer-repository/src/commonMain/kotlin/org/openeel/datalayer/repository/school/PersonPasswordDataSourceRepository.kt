package org.openeel.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.ext.combineWithRemote
import org.openeel.datalayer.ext.updateFromRemoteListIfNeeded
import org.openeel.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import org.openeel.datalayer.school.PersonPasswordDataSource
import org.openeel.datalayer.school.PersonPasswordDataSourceLocal
import org.openeel.datalayer.school.model.PersonPassword
import org.openeel.datalayer.school.writequeue.RemoteWriteQueue
import org.openeel.datalayer.school.writequeue.WriteQueueItem
import org.openeel.datalayer.shared.RepositoryModelDataSource
import org.openeel.libutil.util.time.systemTimeInMillis

class PersonPasswordDataSourceRepository(
    override val local: PersonPasswordDataSourceLocal,
    override val remote: PersonPasswordDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
): PersonPasswordDataSource, RepositoryModelDataSource<PersonPassword> {

    override suspend fun listAll(
        listParams: PersonPasswordDataSource.GetListParams
    ): DataLoadState<List<PersonPassword>> {
        val remote = remote.listAll(listParams)
        local.updateFromRemoteListIfNeeded(
            remote, validationHelper
        )

        return local.listAll(listParams)
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonPasswordDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonPassword>>> {
        return local.listAllAsFlow(loadParams, listParams).combineWithRemote(
            remoteFlow = remote.listAllAsFlow(loadParams, listParams).onEach {
                local.updateFromRemoteListIfNeeded(it, validationHelper)
            }
        )
    }

    override suspend fun store(list: List<PersonPassword>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON_PASSWORD,
                    uid = it.personGuid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}