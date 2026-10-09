package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toClassEntities
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.ClassDataSource
import world.respect.datalayer.school.ClassDataSourceLocal
import world.respect.datalayer.school.model.Clazz
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import kotlin.collections.map
import kotlin.time.Clock

class ClassDatasourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : ClassDataSourceLocal {


    private suspend fun doUpsertClass(
        clazz: Clazz,
    ) {
        val entities = clazz.copy(stored = Clock.System.now()).toEntities(uidNumberMapper)

        schoolDb.getClassPermissionEntityDao().deleteByClassUidNum(
            classUidNum = entities.clazz.cGuidHash
        )
        schoolDb.getClassEntityDao().upsert(entities.clazz)
        schoolDb.getClassPermissionEntityDao().upsertList(
            permissionsList = entities.permissionEntities
        )
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Clazz>> {
        return schoolDb.getClassEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { classEntity ->
            classEntity?.toClassEntities()?.toModel()?.let {
                DataReadyState(it)
            } ?: NoDataLoadedState.notFound()
        }
    }

    override suspend fun findByGuid(
        params: DataLoadParams,
        guid: String
    ): DataLoadState<Clazz> {
        return schoolDb.getClassEntityDao().findByGuid(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(it.toClassEntities().toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): IPagingSourceFactory<Int, Clazz> {
        return IPagingSourceFactory {
            schoolDb.getClassEntityDao().listAsPagingSource(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = params.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                code = params.inviteGuid,
            ).map {
                it.toClassEntities().toModel()
            }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: ClassDataSource.GetListParams
    ): DataLoadState<List<Clazz>> {
        return DataReadyState(
            data = schoolDb.getClassEntityDao().list(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = params.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                code = params.inviteGuid,
            ).map {
                it.toClassEntities().toModel()
            }
        )
    }

    override suspend fun store(list: List<Clazz>) {
        if(list.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { clazz ->
                    //Permission check: if class already exists then check permission including
                    //class permissions granted by role. Otherwise, look for the system permission
                    //to insert a new class.
                    val lastModAndPermissionInDb = schoolDb.getClassEntityDao()
                        .getLastModifiedAndHasPermission(
                            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                            classUidNum = uidNumberMapper(clazz.guid),
                            requiredPermission = PermissionFlags.CLASS_WRITE,
                        )

                    if(!lastModAndPermissionInDb.hasPermission)
                        throw ForbiddenException()

                    doUpsertClass(clazz)
                }
            }
            Napier.d("RPaging/ClassDataSourceDb: store: ${list.size} classes")
        }
    }

    override suspend fun updateLocal(
        list: List<Clazz>,
        forceOverwrite: Boolean
    ) {
        var numUpdated = 0
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { clazz ->
                    val lastModifiedInDb = schoolDb.getClassEntityDao().getLastModifiedByGuid(
                        uidNumberMapper(clazz.guid)
                    ) ?: -1

                    if(forceOverwrite || clazz.lastModified.toEpochMilliseconds() > lastModifiedInDb) {
                        doUpsertClass(clazz)
                        numUpdated++
                    }
                }
            }
        }
        Napier.d("RPaging/ClassDataSourceDb: updatedLocal: ${numUpdated}/${list.size} classes")
    }

    override suspend fun findByUidList(uids: List<String>): List<Clazz> {
        return schoolDb.getClassEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map {
            it.toClassEntities().toModel()
        }
    }
}