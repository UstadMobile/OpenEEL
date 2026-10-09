package world.respect.lib.test.clientservertest

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.SchoolDataSourceLocal
import world.respect.datalayer.UidNumberMapper
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.libxxhash.XXStringHasher
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File

data class SchoolDbDataSourceContext(
    val db: RespectSchoolDatabase,
    val datasource: SchoolDataSourceLocal
)

suspend fun withSchoolDbDataSource(
    dbDir: File,
    dbFilename: String = "school.db",
    schoolUrl: Url,
    stringHasher: XXStringHasher = XXStringHasherCommonJvm(),
    localAuthenticatedUser: AuthenticatedUserPrincipalId = AuthenticatedUserPrincipalId("1"),
    uidMapper: UidNumberMapper = XXHashUidNumberMapper(stringHasher),
    getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase? = null,
    block: suspend SchoolDbDataSourceContext.() -> Unit,
) {
    val schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
        name = File(dbDir, dbFilename).absolutePath
    ).setDriver(BundledSQLiteDriver())
        .build()

    val schoolDataSource = SchoolDataSourceDb(
        schoolDb = schoolDb,
        uidNumberMapper = uidMapper,
        authenticatedUser = localAuthenticatedUser,
        checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
            authenticatedUser = localAuthenticatedUser,
            schoolDb = schoolDb,
            uidNumberMapper = uidMapper,
        ),
        defaultAppCatalogUrl = null,
        json = Json { ignoreUnknownKeys = true },
        schoolUrl = schoolUrl,
        authenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
    )

    block(SchoolDbDataSourceContext(schoolDb, schoolDataSource))
}

/**
 *
 */
fun newLocalSchoolDatabase(
    dir: File,
    schoolUrl: Url,
    stringHasher: XXStringHasher = XXStringHasherCommonJvm(),
    localAuthenticatedUser: AuthenticatedUserPrincipalId,
    uidMapper: UidNumberMapper = XXHashUidNumberMapper(stringHasher),
    getAuthenticatedXapiAgentsUseCase: GetAuthenticatedXapiAgentsUseCase? = null,
): Pair<RespectSchoolDatabase, SchoolDataSourceLocal> {
    val schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
        name = File(dir, "school.db").absolutePath
    ).setDriver(BundledSQLiteDriver())
        .build()

    val schoolDataSource = SchoolDataSourceDb(
        schoolDb = schoolDb,
        uidNumberMapper = uidMapper,
        authenticatedUser = localAuthenticatedUser,
        checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
            authenticatedUser = localAuthenticatedUser,
            schoolDb = schoolDb,
            uidNumberMapper = uidMapper,
        ),
        defaultAppCatalogUrl = null,
        json = Json { ignoreUnknownKeys = true },
        schoolUrl = schoolUrl,
        authenticatedXapiAgentsUseCase = getAuthenticatedXapiAgentsUseCase,
    )

    return Pair(schoolDb, schoolDataSource)
}

suspend fun SchoolDataSourceLocal.insertAdminAndDefaultGrants(
    schoolDb: RespectSchoolDatabase,
    adminPerson: Person = Person(
        guid = "1",
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = listOf(
            PersonRole(true, PersonRoleEnum.SYSTEM_ADMINISTRATOR)
        ),
    ),
    uidNumberMapper: UidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm()),
): Person {
    personDataSource.updateLocal(listOf(adminPerson))
    AddDefaultSchoolPermissionGrantsUseCase(
        schoolDb = schoolDb,
        uidNumberMapper = uidNumberMapper,
    ).invoke()
    return adminPerson
}

