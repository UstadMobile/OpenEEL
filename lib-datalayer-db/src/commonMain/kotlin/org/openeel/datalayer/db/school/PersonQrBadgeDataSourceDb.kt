package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import world.respect.datalayer.school.PersonQrBadgeDataSource
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.model.PersonQrBadge
import kotlin.time.Clock

class PersonQrBadgeDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
) : PersonQrCodeBadgeDataSourceLocal {


    override suspend fun store(list: List<PersonQrBadge>) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()
                list.forEach { personQrBadge ->
                    if(
                        !checkPersonPermissionUseCase(
                            otherPersonUid = personQrBadge.personGuid,
                            otherPersonKnownRole = null,
                            permissionsRequiredByRole = CheckPersonPermissionUseCase.PermissionsRequiredByRole.WRITE_PERMISSIONS,
                        )
                    ) {
                        throw ForbiddenException("Authenticated user does not have permission to set password ${personQrBadge.personGuid}")
                    }
                }

                schoolDb.getPersonQrBadgeEntityDao().upsertAsyncList(
                    list.map { it.copy(stored = now).asEntity(uidNumberMapper) }
                )
            }
        }
    }

    override suspend fun listAll(
        loadParams: DataLoadParams,
        listParams: PersonQrBadgeDataSource.GetListParams
    ): DataLoadState<List<PersonQrBadge>> {
        return DataReadyState(
            data = schoolDb.getPersonQrBadgeEntityDao().findAll(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                personGuidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                includeDeleted = listParams.common.includeDeleted ?: false,
                qrCodeUrl = listParams.qrCodeUrl?.toString(),
            ).map {
                it.asModel()
            }
        )
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonQrBadgeDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonQrBadge>>> {
        return schoolDb.getPersonQrBadgeEntityDao().findAllAsFlow(
            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
            personGuidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
            includeDeleted = listParams.common.includeDeleted ?: false,
            qrCodeUrl = listParams.qrCodeUrl?.toString(),
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel() }
            )
        }
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<PersonQrBadge>> {
        return schoolDb.getPersonQrBadgeEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { personQrEntity ->
            if (personQrEntity != null) {
                DataReadyState(
                    data = personQrEntity.asModel()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun updateLocal(
        list: List<PersonQrBadge>,
        forceOverwrite: Boolean
    ) {
        val timeNow = Clock.System.now()

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonQrBadgeEntityDao().getLastModifiedByUidNum(
                        uidNum = uidNumberMapper(it.personGuid)
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = timeNow).asEntity(uidNumberMapper)
                }
                schoolDb.getPersonQrBadgeEntityDao().upsertAsyncList(toUpdate)
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonQrBadge> {
        return schoolDb.getPersonQrBadgeEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map {
            it.asModel()
        }
    }
}