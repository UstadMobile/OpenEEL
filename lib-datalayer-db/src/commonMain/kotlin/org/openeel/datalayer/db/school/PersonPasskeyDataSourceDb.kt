package org.openeel.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.school.adapters.asEntity
import org.openeel.datalayer.db.school.adapters.asModel
import org.openeel.datalayer.school.PersonPasskeyDataSource.GetListParams
import org.openeel.datalayer.school.PersonPasskeyDataSourceLocal
import org.openeel.datalayer.school.model.PersonPasskey
import org.openeel.lib.dataloadstate.throwable.ForbiddenException
import kotlin.time.Clock

class PersonPasskeyDataSourceDb(
    private val schoolDb: SchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : PersonPasskeyDataSourceLocal {

    override suspend fun listAll(
        listParams: GetListParams
    ): DataLoadState<List<PersonPasskey>> {
        return DataReadyState(
            data = schoolDb.getPersonPasskeyEntityDao().findAll(
                personGuidNumber = uidNumberMapper(authenticatedUser.guid),
                includeRevoked = if (listParams.includeRevoked) 1 else 0,
            ).map {
                it.asModel(authenticatedUser.guid)
            }
        )
    }

    override suspend fun updateLocal(
        list: List<PersonPasskey>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val timeNow = Clock.System.now()

                val toUpdate = list.filter {
                    forceOverwrite || (schoolDb.getPersonPasskeyEntityDao().getLastModifiedByPersonUidAndKeyId(
                        personUidNum = uidNumberMapper(authenticatedUser.guid),
                        passKeyId = it.credentialId
                    ) ?: 0) < it.lastModified.toEpochMilliseconds()
                }.map {
                    it.copy(stored = timeNow).asEntity(uidNumberMapper)
                }

                schoolDb.getPersonPasskeyEntityDao().takeIf { toUpdate.isNotEmpty() }
                    ?.upsertAsync(toUpdate)
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<PersonPasskey> {
        throw IllegalArgumentException("Passkeydatasource: will not find by uid list - not used with RemoteWriteQueue")
    }

    override fun listAllAsFlow(
        listParams: GetListParams
    ): Flow<DataLoadState<List<PersonPasskey>>> {
        return schoolDb.getPersonPasskeyEntityDao().findAllAsFlow(
            personGuidNumber = uidNumberMapper(authenticatedUser.guid),
            includeRevoked = if (listParams.includeRevoked) 1 else 0,
        ).map { list ->
            DataReadyState(
                data = list.map { it.asModel(authenticatedUser.guid) }
            )
        }
    }

    override suspend fun store(list: List<PersonPasskey>) {
        if(list.any { it.personGuid != authenticatedUser.guid })
            throw ForbiddenException("Cannot store passkeys for other user")

        val timeNow = Clock.System.now()
        schoolDb.getPersonPasskeyEntityDao().upsertAsync(
            list.map { it.copy(stored = timeNow).asEntity(uidNumberMapper) }
        )
    }

}