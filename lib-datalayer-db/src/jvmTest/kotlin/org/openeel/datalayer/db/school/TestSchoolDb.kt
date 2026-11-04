package org.openeel.datalayer.db.school

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.UidNumberMapper
import org.openeel.datalayer.db.SchoolDatabase
import org.openeel.datalayer.db.SchoolDataSourceDb
import org.openeel.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.lib.primarykeygen.PrimaryKeyGenerator
import org.openeel.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import org.openeel.lib.xapi.model.XapiAccount
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm
import java.io.File

suspend fun testSchoolDb(
    tempDir: File,
    dbFileName: String = "school.db",
    block: suspend (SchoolDatabase) -> Unit,
) {
    val db = Room.databaseBuilder<SchoolDatabase>(
        File(tempDir, dbFileName).absolutePath
    ).setDriver(BundledSQLiteDriver())
        .build()

    try {
        block(db)
    }finally {
        db.close()
    }
}

suspend fun SchoolDataSourceDb.insertAdmin(
    adminUserUid: String = "1"
) : Person{
    val adminPerson = Person(
        guid = adminUserUid,
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.FEMALE,
        roles = listOf(
            PersonRole(
                isPrimaryRole = true,
                roleEnum = PersonRoleEnum.SYSTEM_ADMINISTRATOR,
            )
        ),
    )
    personDataSource.updateLocal(listOf(adminPerson))
    return adminPerson
}

fun SchoolDatabase.toDataSource(
    authenticatedUserUid: String,
    schoolUrl: Url,
    uidNumberMapper: UidNumberMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm()),
    authenticatedAgents: GetAuthenticatedXapiAgentsUseCase = {
        listOf(
            XapiAgent(
                account = XapiAccount(
                    homePage = schoolUrl.toString(),
                    name = authenticatedUserUid,
                )
            )
        )
    },
): SchoolDataSourceDb {
    val authenticatedUser = AuthenticatedUserPrincipalId(authenticatedUserUid)
    return SchoolDataSourceDb(
        schoolDb = this,
        uidNumberMapper = uidNumberMapper,
        authenticatedUser = authenticatedUser,
        checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
            authenticatedUser = authenticatedUser,
            schoolDb = this,
            uidNumberMapper = uidNumberMapper,
        ),
        json = Json {
            encodeDefaults = false
            ignoreUnknownKeys = true
        },
        primaryKeyGenerator = PrimaryKeyGenerator(SchoolDatabase.TABLE_IDS),
        defaultAppCatalogUrl = "https://respect.world/respect-ds/apps.json",
        schoolUrl = schoolUrl,
        authenticatedXapiAgentsUseCase = authenticatedAgents,
    )
}



