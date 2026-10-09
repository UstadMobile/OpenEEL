package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.asEntity
import world.respect.datalayer.db.school.adapters.asModel
import world.respect.datalayer.school.PersonPasskeyDataSource.GetListParams
import world.respect.datalayer.school.PersonPasskeyDataSourceLocal
import world.respect.datalayer.school.model.PersonPasskey
import world.respect.lib.dataloadstate.throwable.ForbiddenException
import kotlin.time.Clock

class PersonPasskeyDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
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