package org.openeel.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.asEntity
import org.openeel.datalayer.db.school.adapters.asModel
import org.openeel.datalayer.exceptions.ForbiddenException
import org.openeel.datalayer.school.PersonQrCodeBadgeDataSourceLocal
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.model.PersonQrBadge
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