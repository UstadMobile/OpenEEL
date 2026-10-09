package world.respect.datalayer.db.school

import androidx.room.Transactor
import androidx.room.useWriterConnection
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.lib.dataloadstate.DataLoadMetaInfo
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.DataReadyState
import world.respect.lib.dataloadstate.NoDataLoadedState
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.school.adapters.toEntities
import world.respect.datalayer.db.school.adapters.toModel
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.EnrollmentDataSourceLocal
import world.respect.datalayer.school.model.Enrollment
import world.respect.datalayer.school.model.EnrollmentRoleEnum
import world.respect.datalayer.school.model.PermissionFlags
import world.respect.datalayer.shared.maxLastModifiedOrNull
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import world.respect.lib.dataloadstate.throwable.ForbiddenException
import world.respect.libutil.util.time.atStartOfDayInMillisUtc
import kotlin.collections.map
import kotlin.time.Clock
import kotlin.time.Instant

class EnrollmentDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    @Suppress("unused")
    private val authenticatedUser: AuthenticatedUserPrincipalId,
) : EnrollmentDataSourceLocal {

    private val logPrefix: String by lazy {
        "EnrollmentDataSourceDb(${authenticatedUser.guid})"
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Enrollment> {
        return schoolDb.getEnrollmentEntityDao().findByGuid(
            uidNumberMapper(guid)
        )?.let {
            DataReadyState(it.toModel())
        } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(
        loadParams: DataLoadParams,
        guid: String
    ): Flow<DataLoadState<Enrollment>> {
        return schoolDb.getEnrollmentEntityDao().findByGuidAsFlow(
            uidNumberMapper(guid)
        ).map {
            it?.let {
                DataReadyState(it.toModel())
            } ?: NoDataLoadedState.notFound()
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): DataLoadState<List<Enrollment>> {
        val queryTime = Clock.System.now()
        val permissionsLastModified = Instant.fromEpochMilliseconds(
            schoolDb.getPersonEntityDao().getMostRecentPermissionChangeTime(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid)
            )
        )

        return schoolDb.getEnrollmentEntityDao().list(
            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
            since = listParams.common.since?.toEpochMilliseconds().takeIf {
                listParams.common.sinceIfPermissionsNotChangedSince == permissionsLastModified
            } ?: 0,
            uidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
            classUidNum = listParams.classUid?.let { uidNumberMapper(it) } ?: 0,
            classUidRoleFlag = listParams.role?.flag ?: 0,
            personUidNum = listParams.personUid?.let { uidNumberMapper(it) } ?: 0,
            includeDeleted = listParams.common.includeDeleted ?: false,
            activeOnDayInUtcMs = listParams.activeOnDay?.atStartOfDayInMillisUtc() ?: 0,
            notRemovedBefore = 0,
            sortByFlag = listParams.orderBy.orderOption.flag,
        ).map { it.toModel() }.let {
            DataReadyState(
                data = it,
                metaInfo = DataLoadMetaInfo(
                    lastModified = it.maxLastModifiedOrNull()?.toEpochMilliseconds() ?: -1,
                    lastStored = it.maxLastStoredOrNull()?.toEpochMilliseconds() ?: -1,
                    consistentThrough = queryTime,
                    permissionsLastModified = permissionsLastModified,
                )
            )
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        listParams: EnrollmentDataSource.GetListParams
    ): IPagingSourceFactory<Int, Enrollment> {
        return IPagingSourceFactory {
            schoolDb.getEnrollmentEntityDao().listAsPagingSource(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = listParams.common.since?.toEpochMilliseconds() ?: 0,
                uidNum = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                classUidNum = listParams.classUid?.let { uidNumberMapper(it) } ?: 0,
                classUidRoleFlag = listParams.role?.flag ?: 0,
                personUidNum = listParams.personUid?.let { uidNumberMapper(it) } ?: 0,
                includeDeleted = listParams.common.includeDeleted ?: false,
                activeOnDayInUtcMs = listParams.activeOnDay?.atStartOfDayInMillisUtc() ?: 0,
                notRemovedBefore = 0,
                sortByFlag = listParams.orderBy.orderOption.flag,
            ).map(
                tag = { "EnrollmentDataSourceDb/list params=$listParams" }
            ) {
                it.toModel()
            }
        }
    }

    override suspend fun store(list: List<Enrollment>) {
        if(list.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()
                list.forEach { enrollment ->
                    val classPermissionResult = schoolDb.getClassEntityDao().getLastModifiedAndHasPermission(
                        authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                        classUidNum = uidNumberMapper(enrollment.classUid),
                        requiredPermission = when(enrollment.role) {
                            EnrollmentRoleEnum.STUDENT, EnrollmentRoleEnum.PENDING_STUDENT -> {
                                PermissionFlags.CLASS_WRITE_STUDENT_ENROLLMENT
                            }
                            else -> PermissionFlags.CLASS_WRITE_TEACHER_ENROLLMENT
                        }
                    )

                    if(!enrollment.role.requiresApproval && !classPermissionResult.hasPermission)
                        throw ForbiddenException("$logPrefix no permission to enrol in " +
                                "class ${enrollment.classUid}")

                    schoolDb.getEnrollmentEntityDao().upsert(
                        listOf(
                            enrollment.copy(
                                stored = now
                            ).toEntities(uidNumberMapper)
                        )
                    )
                }
            }
        }
    }

    override suspend fun updateLocal(
        list: List<Enrollment>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                val now = Clock.System.now()
                schoolDb.getEnrollmentEntityDao().upsert(
                    enrolments = list.filter { enrollment ->
                        forceOverwrite || schoolDb.getEnrollmentEntityDao().getLastModifiedByUidNum(
                            uidNumberMapper(enrollment.uid)
                        ).let { it ?: 0L } < enrollment.lastModified.toEpochMilliseconds()
                    }.map {
                        it.copy(stored = now).toEntities(uidNumberMapper)
                    }
                )
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<Enrollment> {
        return schoolDb.getEnrollmentEntityDao().findByUidNumList(
            uids.map { uidNumberMapper(it) }
        ).map { it.toModel() }
    }


}