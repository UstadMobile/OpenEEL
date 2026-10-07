package org.openeel.server.domain.school.add

import io.ktor.http.HttpStatusCode
import kotlinx.serialization.Serializable
import org.koin.core.component.KoinComponent
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.datalayer.schooldirectory.SchoolDirectoryDataSourceLocal
import org.openeel.datalayer.respect.model.SchoolDirectoryEntry
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import org.openeel.lib.dataloadstate.ext.dataOrNull
import org.openeel.datalayer.school.ext.newUserInviteUid
import org.openeel.datalayer.school.model.Invite2
import org.openeel.datalayer.school.model.NewUserInvite
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.schooldirectory.SchoolDirectoryEntryDataSourceLocal
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.server.util.ext.HttpStatusException
import org.openeel.shared.domain.account.RespectAccount
import org.openeel.shared.domain.account.invite.CreateInviteUseCase
import org.openeel.shared.domain.account.setpassword.EncryptPersonPasswordUseCase
import org.openeel.shared.util.di.RespectAccountScopeId
import org.openeel.shared.util.di.SchoolDirectoryEntryScopeId
import kotlin.time.Clock

/**
 * Used by command line client, potentially web admin UI to add a realm.
 */
class AddSchoolUseCase(
    private val directoryDataSource: SchoolDirectoryDataSourceLocal,
    private val schoolDirectoryEntryDataSource: SchoolDirectoryEntryDataSourceLocal,
    private val encryptPasswordUseCase: EncryptPersonPasswordUseCase,
) : KoinComponent {

    @Serializable
    data class AddSchoolRequest(
        val school: SchoolDirectoryEntry,
        val dbUrl: String,
        val adminUsername: String? = null,
        val adminPassword: String? = null,
    )

    suspend operator fun invoke(
        requests: List<AddSchoolRequest>
    ) {
        requests.forEach { request ->
            val schoolToAdd = request.school.copy(
                self = request.school.self.normalizeForEndpoint()
            )

            // Check if school with this URL already exists
            val existingSchools = schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
                url = schoolToAdd.self
            )

            if (existingSchools.dataOrNull() != null) {
                throw HttpStatusException(
                    "A school with URL '${request.dbUrl}' already exists",
                    HttpStatusCode.Conflict
                )
            }

            schoolDirectoryEntryDataSource.updateLocal(listOf(schoolToAdd))

            directoryDataSource.setServerManagedSchoolConfig(
                schoolToAdd, request.dbUrl
            )
            val adminGuid = "1"
            val schoolScope = getKoin().createScope<SchoolDirectoryEntry>(
                SchoolDirectoryEntryScopeId(
                    schoolToAdd.self, null
                ).scopeId
            )

            if (request.adminPassword != null) {
                val accountScope = getKoin().createScope<RespectAccount>(
                    RespectAccountScopeId(
                        schoolToAdd.self, AuthenticatedUserPrincipalId(adminGuid)
                    ).scopeId
                )

                accountScope.linkTo(schoolScope)

                val schoolDataSource: SchoolDataSourceLocal = accountScope.get()
                val adminPerson = Person(
                    guid = adminGuid,
                    username = request.adminUsername,
                    givenName = "Admin",
                    familyName = "Admin",
                    gender = PersonGenderEnum.UNSPECIFIED,
                    roles = listOf(
                        PersonRole(
                            isPrimaryRole = true,
                            roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
                        )
                    )
                )

                //Use updateLocal to bypass permission check for adding first user
                schoolDataSource.personDataSource.updateLocal(listOf(adminPerson))

                schoolDataSource.personPasswordDataSource.store(
                    listOf(
                        encryptPasswordUseCase(
                            EncryptPersonPasswordUseCase.Request(
                                personGuid = adminPerson.guid,
                                password = request.adminPassword,
                            )
                        )
                    )
                )
            }

            //insert default SchoolPermissionGrants
            val addDefaultGrantsUseCase: AddDefaultSchoolPermissionGrantsUseCase = schoolScope.get()
            addDefaultGrantsUseCase()

            val createInviteUseCase: CreateInviteUseCase = schoolScope.get()

            //Create invites for system roles
            PersonRoleEnum.entries.forEach { personRole ->
                createInviteUseCase(
                    invite = NewUserInvite(
                        uid = personRole.newUserInviteUid,
                        code = Invite2.newRandomCode(),
                        role = personRole,
                        approvalRequiredAfter = Clock.System.now(),
                    )
                )
            }
        }
    }

    companion object {
        const val DEFAULT_ADMIN_USERNAME = "admin"
    }

}