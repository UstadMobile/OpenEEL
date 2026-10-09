package world.respect.datalayer.db.school

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.school.adapters.toEntity
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.db.school.daos.SchoolPermissionGrantDao
import world.respect.datalayer.school.SchoolPermissionGrantDataSource
import world.respect.datalayer.school.SchoolPermissionGrantDataSourceLocal
import world.respect.datalayer.school.ext.assertPersonHasRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.school.model.SchoolPermissionGrant
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map

class SchoolPermissionGrantDataSourceDb(
    private val schoolPermissionGrantDao: SchoolPermissionGrantDao,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val getAuthenticatedPersonUseCase: GetAuthenticatedPersonUseCase,
) : SchoolPermissionGrantDataSourceLocal {

    override suspend fun store(list: List<SchoolPermissionGrant>) {
        getAuthenticatedPersonUseCase().assertPersonHasRole(PersonRoleEnum.SYSTEM_ADMINISTRATOR)
        schoolPermissionGrantDao.upsert(list.map { it.toEntity(uidNumberMapper) })
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<SchoolPermissionGrant>> {
        val uidNum = uidNumberMapper(guid)
        return schoolPermissionGrantDao.findByUidNumAsFlow(uidNum).map { entity ->
            DataLoadState.readyOrNotFoundIfNull(entity?.toModel())
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<SchoolPermissionGrant> {
        val uidNum = uidNumberMapper(guid)
        return DataLoadState.readyOrNotFoundIfNull(
            data = schoolPermissionGrantDao.findByUidNum(uidNum)?.toModel()
        )
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: SchoolPermissionGrantDataSource.GetListParams,
    ): IPagingSourceFactory<Int, SchoolPermissionGrant> = IPagingSourceFactory {
        schoolPermissionGrantDao.listAsPagingSource(
            uidNum = params.common.guid?.let { uidNumberMapper(it) } ?: 0
        ).map {
            it.toModel()
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: SchoolPermissionGrantDataSource.GetListParams
    ): DataLoadState<List<SchoolPermissionGrant>> {
        return DataReadyState(
            data = schoolPermissionGrantDao.list(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                uidNum = params.common.guid?.let { uidNumberMapper(it) } ?: 0
            ).map { it.toModel() }
        )
    }

    override suspend fun updateLocal(
        list: List<SchoolPermissionGrant>,
        forceOverwrite: Boolean
    ) {
        schoolPermissionGrantDao.upsert(
            entities = list.filter {
                forceOverwrite || schoolPermissionGrantDao.getLastModifiedByUidNum(
                    uidNumberMapper(it.uid)
                ).let { it ?: 0 } < it.lastModified.toEpochMilliseconds()
            }.map { it.toEntity(uidNumberMapper) }
        )
    }

    override suspend fun findByUidList(uids: List<String>): List<SchoolPermissionGrant> {
        return schoolPermissionGrantDao.findByUidNums(
            uids.map(uidNumberMapper::invoke)
        ).map {
            it.toModel()
        }
    }
}
