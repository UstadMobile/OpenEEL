package org.openeel.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.DataLoadState
import org.openeel.lib.dataloadstate.DataReadyState
import org.openeel.lib.dataloadstate.NoDataLoadedState
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.school.adapters.toClassEntities
import org.openeel.datalayer.db.school.adapters.toEntities
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.exceptions.ForbiddenException
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.datalayer.school.ClassDataSourceLocal
import org.openeel.datalayer.school.model.Clazz
import org.openeel.datalayer.school.model.PermissionFlags
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.map
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