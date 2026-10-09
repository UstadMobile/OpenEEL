package org.openeel.xapi.ipc.server

import android.app.Application
import androidx.room.Room
import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.SchoolDataSourceLocal
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.SchoolDataSourceDb
import org.openeel.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import org.openeel.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.app.userdirectory.model.PersonRoleEnum
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm

class IpcTestApplication: Application(), XapiResourceProvider{

    internal val schoolDatabase: RespectSchoolDatabase by lazy {
        Room.databaseBuilder<RespectSchoolDatabase>(
            this, "school_db"
        ).build()
    }

    internal val adminUserUid = "1"

    internal val json = Json { encodeDefaults = false }

    internal val schoolUrl = Url("http://localhost/")

    internal val authUser = AuthenticatedUserPrincipalId(adminUserUid)

    internal val numMapper = XXHashUidNumberMapper(XXStringHasherCommonJvm())

    internal val adminPerson = Person(
        guid = adminUserUid,
        givenName = "Admin",
        familyName = "User",
        gender = PersonGenderEnum.UNSPECIFIED,
        roles = listOf(
            PersonRole(true, PersonRoleEnum.SYSTEM_ADMINISTRATOR)
        ),
    )

    internal val schoolDataSource: SchoolDataSourceLocal by lazy {
        SchoolDataSourceDb(
            schoolDb = schoolDatabase,
            uidNumberMapper = numMapper,
            authenticatedUser = authUser,
            checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
                authenticatedUser = authUser,
                schoolDb = schoolDatabase,
                uidNumberMapper = numMapper,
            ),
            json = json,
            defaultAppCatalogUrl = "http://localhost/not-used-here-buddy",
            schoolUrl = schoolUrl,
        )
    }

    internal var useDefaultPermissions = true

    suspend fun insertAdminAndDefaultGrants() {
        schoolDataSource.personDataSource.updateLocal(listOf(adminPerson))
        if(useDefaultPermissions) {
            AddDefaultSchoolPermissionGrantsUseCase(
                schoolDb = schoolDatabase,
                uidNumberMapper = numMapper,
            ).invoke()
        }
    }


    override suspend fun provideXapiResource(
        endpoint: Url,
        authentication: String?
    ): XapiResource {
        return schoolDataSource.xapiResource
    }

}