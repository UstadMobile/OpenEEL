package org.openeel.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.asEntity
import org.openeel.datalayer.db.school.adapters.asModel
import org.openeel.datalayer.exceptions.ForbiddenException
import org.openeel.datalayer.school.PersonPasswordDataSource
import org.openeel.datalayer.school.PersonPasswordDataSourceLocal
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.model.PersonPassword
import kotlin.time.Clock

class PersonPasswordDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : PersonPasswordDataSourceLocal{

    override suspend fun store(list: List<PersonPassword>) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()
                list.forEach { personPassword ->
                    if(
                        !checkPersonPermissionUseCase(
                            otherPersonUid = personPassword.personGuid,
                            otherPersonKnownRole = null,
                            permissionsRequiredByRole = CheckPersonPermissionUseCase.PermissionsRequiredByRole.WRITE_PERMISSIONS,
                        )
                    ) {
                        throw ForbiddenException("Authenticated user does not have permission to set password ${personPassword.personGuid}")
                    }
                }

                schoolDb.getPersonPasswordEntityDao().upsertAsyncList(
                    list = list.map { it.copy(stored = now).asEntity(uidNumberMapper) }
                )
            }
        }
    }

    override suspend fun updateLocal(
        list: List<PersonPassword>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()
                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonPasswordEntityDao().getLastModifiedByPersonUidNum(
                        uidNum = uidNumberMapper(it.personGuid)
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = now).asEntity(uidNumberMapper)
                }

                schoolDb.getPersonPasswordEntityDao().upsertAsyncList(toUpdate)
            }
        }
    }

    override suspend fun listAll(
        listParams: PersonPasswordDataSource.GetListParams
    ): DataLoadState<List<PersonPassword>> {
        return DataReadyState(
            data = schoolDb.getPersonPasswordEntityDao().findAll(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                personGuidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
            ).map {
                it.asModel()
            }
        )
    }

    override fun listAllAsFlow(
        loadParams: DataLoadParams,
        listParams: PersonPasswordDataSource.GetListParams
    ): Flow<DataLoadState<List<PersonPassword>>> {
        return schoolDb.getPersonPasswordEntityDao().findAllAsFlow(
            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
            personGuidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel() }
            )
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonPassword> {
        return schoolDb.getPersonPasswordEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map {
            it.asModel()
        }
    }
}