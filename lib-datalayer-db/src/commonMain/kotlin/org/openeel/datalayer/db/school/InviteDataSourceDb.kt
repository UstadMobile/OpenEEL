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
import org.openeel.datalayer.db.school.adapters.toEntity
import org.openeel.datalayer.db.school.adapters.toModel
import org.openeel.datalayer.exceptions.ForbiddenException
import org.openeel.datalayer.school.InviteDataSource
import org.openeel.datalayer.school.InviteDataSourceLocal
import org.openeel.datalayer.school.domain.CheckPersonPermissionUseCase
import org.openeel.datalayer.school.model.ClassInvite
import org.openeel.datalayer.school.model.EnrollmentRoleEnum
import org.openeel.datalayer.school.model.FamilyMemberInvite
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.datalayer.school.model.PermissionFlags
import org.openeel.datalayer.shared.paging.IPagingSourceFactory
import org.openeel.datalayer.shared.paging.map
import kotlin.time.Clock

class InviteDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : InviteDataSourceLocal {


    override suspend fun findByGuid(guid: String): DataLoadState<Invite2> {
        return schoolDb.getInviteEntityDao().findByGuidHash(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun findByUidAsFlow(
        uid: String,
        loadParams: DataLoadParams
    ): Flow<DataLoadState<Invite2>> {
        return schoolDb.getInviteEntityDao().findByGuidHashAsFlow(
            guidHash = uidNumberMapper(uid)
        ).map {
            it?.let {
                DataReadyState(it.toModel())
            } ?: NoDataLoadedState.notFound()
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: InviteDataSource.GetListParams
    ): IPagingSourceFactory<Int, Invite2> {
        return IPagingSourceFactory {
            schoolDb.getInviteEntityDao().listAsPagingSource(
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                code = params.inviteCode,
            ).map {
                it.toModel()
            }
        }
    }

    override suspend fun store(list: List<Invite2>) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { inviteToStore ->
                    val inviteInDb = schoolDb.getInviteEntityDao().findByGuidHash(
                        guidHash = uidNumberMapper(inviteToStore.uid)
                    )?.toModel()

                    when(inviteToStore) {
                        is NewUserInvite -> {
                            if(
                                !checkPersonPermissionUseCase(
                                    otherPersonUid = "0",
                                    otherPersonKnownRole = inviteToStore.role,
                                    permissionsRequiredByRole = CheckPersonPermissionUseCase.PermissionsRequiredByRole.WRITE_PERMISSIONS
                                )
                            ) {
                                throw ForbiddenException(
                                    "Authenticated user does not have write permission required for invite ${inviteToStore.uid}"
                                )
                            }
                        }

                        is ClassInvite -> {
                            val hasRequiredClassPermission = schoolDb.getClassEntityDao().getLastModifiedAndHasPermission(
                                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                                classUidNum = uidNumberMapper(inviteToStore.classUid),
                                requiredPermission = when(inviteToStore.role) {
                                    EnrollmentRoleEnum.STUDENT, EnrollmentRoleEnum.PENDING_STUDENT -> {
                                        PermissionFlags.CLASS_WRITE_STUDENT_ENROLLMENT
                                    }
                                    else -> PermissionFlags.CLASS_WRITE_TEACHER_ENROLLMENT
                                }
                            ).hasPermission

                            if(!hasRequiredClassPermission) {
                                throw ForbiddenException(
                                    "Authenticated user does not have write permission required for class invite ${inviteToStore.uid}"
                                )
                            }
                        }

                        else -> {
                            //Family member invite
                            val knownPersonUid = (inviteToStore as? FamilyMemberInvite)?.personUid ?: "0"
                        }
                    }
                }

                val timeNow = Clock.System.now()
                schoolDb.getInviteEntityDao().insertAll(
                    list.map { it.toEntity(uidNumberMapper).copy(iStored = timeNow) }
                )
            }
        }
    }

    override suspend fun updateLocal(list: List<Invite2>, forceOverwrite: Boolean) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val timeNow = Clock.System.now()
                schoolDb.getInviteEntityDao().insertAll(
                    invites = list.filter { invite ->
                        forceOverwrite || schoolDb.getInviteEntityDao().getLastModifiedByGuid(
                            uidNumberMapper(invite.uid)
                        ).let { it ?: 0L } < invite.lastModified.toEpochMilliseconds()
                    }.map {
                        it.toEntity(uidNumberMapper).copy(iStored = timeNow)
                    }
                )
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<Invite2> {
        val uidNums = uids.map { uidNumberMapper(it) }
        return schoolDb.getInviteEntityDao().findByUidList(uidNums)
            .map { it.toModel() }
    }

    override suspend fun findByCode(code: String): DataLoadState<Invite2> {
        return schoolDb.getInviteEntityDao().getInviteByInviteCode(
            code
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }

    fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Invite2>> {
        return schoolDb.getInviteEntityDao()
            .findByGuidHashAsFlow(uidNumberMapper(guid))
            .map { entity ->
                if (entity != null) {
                    DataReadyState(entity.toModel())
                } else {
                    NoDataLoadedState.notFound()
                }
            }
    }
}

