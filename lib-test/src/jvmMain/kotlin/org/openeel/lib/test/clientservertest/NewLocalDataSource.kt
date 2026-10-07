package org.openeel.lib.test.clientservertest

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.SchoolDataSourceDb
import org.openeel.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import org.openeel.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.libxxhash.XXStringHasher
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File

data class SchoolDbDataSourceContext(
    val db: SchoolDatabase,
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
    val schoolDb = Room.databaseBuilder<SchoolDatabase>(
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
): Pair<SchoolDatabase, SchoolDataSourceLocal> {
    val schoolDb = Room.databaseBuilder<SchoolDatabase>(
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
    schoolDb: SchoolDatabase,
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

