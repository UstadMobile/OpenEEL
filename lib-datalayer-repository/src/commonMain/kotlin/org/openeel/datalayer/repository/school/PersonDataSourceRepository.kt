package world.respect.datalayer.repository.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.ext.combineWithRemote
import world.respect.datalayer.ext.updateFromRemoteIfNeeded
import world.respect.datalayer.networkvalidation.ExtendedDataSourceValidationHelper
import world.respect.datalayer.repository.shared.paging.RepositoryPagingSourceFactory
import world.respect.datalayer.repository.shared.paging.loadAndUpdateLocal2
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.school.writequeue.RemoteWriteQueue
import world.respect.datalayer.school.writequeue.WriteQueueItem
import world.respect.datalayer.shared.RepositoryModelDataSource
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.libutil.util.time.systemTimeInMillis

class PersonDataSourceRepository(
    override val local: PersonDataSourceLocal,
    override val remote: PersonDataSource,
    private val validationHelper: ExtendedDataSourceValidationHelper,
    private val remoteWriteQueue: RemoteWriteQueue,
) : PersonDataSource, RepositoryModelDataSource<Person> {

    override suspend fun findByUsername(username: String): Person? {
        return local.findByUsername(username)
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        if(!loadParams.onlyIfCached) {
            val remote = remote.findByGuid(loadParams, guid)
            local.updateFromRemoteIfNeeded(
                remote, validationHelper
            )
        }

        return local.findByGuid(loadParams, guid)
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        return local.findByGuidAsFlow(guid).combineWithRemote(
            remoteFlow = remote.findByGuidAsFlow(guid).onEach {
                local.updateFromRemoteIfNeeded(it, validationHelper)
            }
        )
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> {
        return local.listAsFlow(loadParams, params)
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): DataLoadState<List<Person>> {
        val remote = remote.list(loadParams, params)
        if(remote is DataReadyState) {
            local.updateLocal(remote.data)
            validationHelper.updateValidationInfo(remote.metaInfo)
        }

        return local.list(loadParams, params).combineWithRemote(remote)
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, Person> {
        val remoteSource = remote.takeIf { !loadParams.onlyIfCached }?.listAsPagingSource(
            loadParams = loadParams,
            params = params.copy(
                common = params.common.copy(includeDeleted = true),
                inClassOnDay = null,
            )
        )?.invoke()

        return RepositoryPagingSourceFactory(
            onRemoteLoad = { remoteLoadParams ->
                remoteSource?.loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal,
                )
            },
            local = local.listAsPagingSource(loadParams, params),
            tag = { "Repo.listAsPaging(params=$params)" },
        )
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams
    ): IPagingSourceFactory<Int, PersonListDetails> {
        return RepositoryPagingSourceFactory(
            onRemoteLoad = { remoteLoadParams ->
                remote.listAsPagingSource(loadParams, listParams).invoke().loadAndUpdateLocal2(
                    remoteLoadParams, local::updateLocal,
                )
            },
            local = local.listDetailsAsPagingSource(loadParams, listParams),
            tag = { "Repo.listDetailsAsPaging(params=$listParams)" },
        )
    }

    override suspend fun store(list: List<Person>) {
        local.store(list)
        val timeNow = systemTimeInMillis()
        remoteWriteQueue.add(
            list.map {
                WriteQueueItem(
                    model = WriteQueueItem.Model.PERSON,
                    uid = it.guid,
                    timeQueued = timeNow,
                )
            }
        )
    }
}