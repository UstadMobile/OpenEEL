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
import world.respect.datalayer.db.school.adapters.toPersonEntities
import world.respect.datalayer.exceptions.ForbiddenException
import world.respect.datalayer.school.PersonDataSource
import world.respect.datalayer.school.PersonDataSourceLocal
import world.respect.datalayer.school.domain.CheckPersonPermissionUseCase
import world.respect.datalayer.school.ext.primaryRole
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.composites.PersonListDetails
import world.respect.datalayer.shared.maxLastModifiedOrNull
import world.respect.datalayer.shared.maxLastStoredOrNull
import world.respect.datalayer.shared.paging.IPagingSourceFactory
import world.respect.datalayer.shared.paging.map
import world.respect.libutil.util.time.atStartOfDayInMillisUtc
import kotlin.time.Clock

class PersonDataSourceDb(
    private val schoolDb: RespectSchoolDatabase,
    private val uidNumberMapper: UidNumberMapper,
    private val authenticatedUser: AuthenticatedUserPrincipalId,
    private val checkPersonPermissionUseCase: CheckPersonPermissionUseCase,
): PersonDataSourceLocal {


    private suspend fun doUpsertPerson(
        person: Person
    ) {
        val entities = person.copy(stored = Clock.System.now()).toEntities(uidNumberMapper)

        schoolDb.getPersonEntityDao().insert(entities.personEntity)

        schoolDb.getPersonRoleEntityDao().deleteByPersonGuidHash(
            entities.personEntity.pGuidHash
        )
        schoolDb.getPersonRoleEntityDao().upsertList(
            entities.personRoleEntities
        )

        schoolDb.getPersonRelatedPersonEntityDao().deleteByPersonUidNum(
            entities.personEntity.pGuidHash
        )
        schoolDb.getPersonRelatedPersonEntityDao().upsert(
            entities.relatedPersonEntities
        )
    }

    override suspend fun store(list: List<Person>) {
        if(list.isEmpty())
            return

        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE) {
                list.forEach { personToStore ->
                    val personInDb = schoolDb.getPersonEntityDao().findByGuidNum(
                        uidNumberMapper(personToStore.guid)
                    )?.toPersonEntities()?.toModel()

                    //Check that if person is in db, role is not being changed.
                    if(personInDb != null &&
                        (personInDb.primaryRole() != personToStore.primaryRole() ||
                                personToStore.roles.size != 1)
                    ) {
                        throw ForbiddenException("Role cannot be changed, and must have one role")
                    }

                    if(
                        !checkPersonPermissionUseCase(
                            otherPersonUid = personToStore.guid,
                            otherPersonKnownRole = personToStore.primaryRole(),
                            permissionsRequiredByRole = CheckPersonPermissionUseCase.PermissionsRequiredByRole.WRITE_PERMISSIONS
                        )
                     ) {
                        throw ForbiddenException("Authenticated user does not have permission to store ${personToStore.guid}")
                    }

                    //Check that roles have not been change
                    doUpsertPerson(personToStore)
                }
            }
        }
    }

    override suspend fun findByUsername(username: String): Person? {
        return schoolDb.getPersonEntityDao().findByUsername(username)?.toPersonEntities()?.toModel()
    }

    override suspend fun findByGuid(
        loadParams: DataLoadParams,
        guid: String
    ): DataLoadState<Person> {
        return schoolDb.getPersonEntityDao().findByGuidNum(uidNumberMapper(guid))
            ?.toPersonEntities()?.toModel()?.let { DataReadyState(it) } ?: NoDataLoadedState.notFound()
    }

    override fun findByGuidAsFlow(guid: String): Flow<DataLoadState<Person>> {
        return schoolDb.getPersonEntityDao().findByGuidHashAsFlow(
            uidNumberMapper(guid)
        ).map { personEntity ->
            if(personEntity != null) {
                DataReadyState(
                    data = personEntity.toPersonEntities().toModel()
                )
            } else {
                NoDataLoadedState(NoDataLoadedState.Reason.NOT_FOUND)
            }
        }
    }

    override suspend fun updateLocal(
        list: List<Person>,
        forceOverwrite: Boolean
    ) {
        schoolDb.useWriterConnection { con ->
            con.withTransaction(Transactor.SQLiteTransactionType.IMMEDIATE){
                list.filter { person ->
                    forceOverwrite || schoolDb.getPersonEntityDao().getLastModifiedByGuid(
                        uidNumberMapper(person.guid)
                    ).let { it ?: 0L } < person.lastModified.toEpochMilliseconds()
                }.forEach { person ->
                    doUpsertPerson(person)
                }
            }
        }
    }

    override suspend fun findByUidList(uids: List<String>): List<Person> {
        return schoolDb.getPersonEntityDao().findByUidList(
            uids.map { uidNumberMapper(it) }
        ).map { it.toPersonEntities().toModel() }
    }

    override fun listAsFlow(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): Flow<DataLoadState<List<Person>>> {
        return schoolDb.getPersonEntityDao().listAsFlow(
            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
            inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
            inClassOnDayInUtcMs = params.inClassOnDay?.atStartOfDayInMillisUtc() ?: 0,
            filterByName = params.filterByName,
            filterByPersonRole = params.filterByPersonRole?.flag ?: 0,
            filterByPersonStatus = params.filterByPersonStatus?.flag ?: 0,
            includeRelated = params.includeRelated,
            includeDeleted = params.common.includeDeleted ?: false,
        ).map { list ->
            DataReadyState(
                data = list.map {
                    it.toPersonEntities().toModel()
                }
            )
        }
    }

    override fun listAsPagingSource(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, Person> {
        return IPagingSourceFactory {
            schoolDb.getPersonEntityDao().listAsPagingSource(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = params.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
                inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
                inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
                inClassOnDayInUtcMs = params.inClassOnDay?.atStartOfDayInMillisUtc() ?: 0,
                filterByName = params.filterByName,
                filterByPersonRole = params.filterByPersonRole?.flag ?: 0,
                filterByPersonStatus = params.filterByPersonStatus?.flag ?: 0,
                includeRelated = params.includeRelated,
                includeDeleted = params.common.includeDeleted ?: false,
                excludeSharedSchoolDevice = params.excludeSharedSchoolDevice,
            ).map(tag = { "PersonDataSourceDb/listAsPagingSource(params=$params)" }) {
                it.toPersonEntities().toModel()
            }
        }
    }

    override suspend fun list(
        loadParams: DataLoadParams,
        params: PersonDataSource.GetListParams,
    ): DataLoadState<List<Person>> {
        val queryTime = Clock.System.now()
        val data = schoolDb.getPersonEntityDao().list(
            authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
            since = params.common.since?.toEpochMilliseconds() ?: 0,
            guidHash = params.common.guid?.let { uidNumberMapper(it) } ?: 0,
            inClazzGuidHash = params.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
            inClazzRoleFlag = params.filterByEnrolmentRole?.flag ?: 0,
            inClassOnDayInUtcMs = params.inClassOnDay?.atStartOfDayInMillisUtc() ?: 0,
            filterByName = params.filterByName,
            filterByPersonRole = params.filterByPersonRole?.flag ?: 0,
            includeRelated = params.includeRelated,
            includeDeleted = params.common.includeDeleted ?: false,
        ).map {
            it.toPersonEntities().toModel()
        }

        return DataReadyState(
            data = data,
            metaInfo = DataLoadMetaInfo(
                lastModified = data.maxLastModifiedOrNull()?.toEpochMilliseconds() ?: -1,
                lastStored = data.maxLastStoredOrNull()?.toEpochMilliseconds() ?: -1,
                consistentThrough = queryTime,
            )
        )
    }

    override fun listDetailsAsPagingSource(
        loadParams: DataLoadParams,
        listParams: PersonDataSource.GetListParams,
    ): IPagingSourceFactory<Int, PersonListDetails> {
        return IPagingSourceFactory {
            schoolDb.getPersonEntityDao().findAllListDetailsAsPagingSource(
                authenticatedPersonUidNum = uidNumberMapper(authenticatedUser.guid),
                since = listParams.common.since?.toEpochMilliseconds() ?: 0,
                guidHash = listParams.common.guid?.let { uidNumberMapper(it) } ?: 0,
                inClazzGuidHash = listParams.filterByClazzUid?.let { uidNumberMapper(it) } ?: 0,
                inClazzRoleFlag = listParams.filterByEnrolmentRole?.flag ?: 0,
                filterByName = listParams.filterByName,
                filterByPersonRole = listParams.filterByPersonRole?.flag ?: 0,
                filterByPersonStatus = listParams.filterByPersonStatus?.flag ?: 0,
                includeRelated = listParams.includeRelated,
                excludeSharedSchoolDevice = listParams.excludeSharedSchoolDevice,
            )
        }
    }

}