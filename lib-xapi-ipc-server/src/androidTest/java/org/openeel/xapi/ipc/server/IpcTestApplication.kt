package world.respect.xapi.ipc.server

import android.app.Application
import androidx.room.Room
import io.github.reactivecircus.cache4k.Cache
import io.ktor.http.Url
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import world.respect.datalayer.AuthenticatedUserPrincipalId
import world.respect.datalayer.db.RespectSchoolDatabase
import world.respect.datalayer.db.SchoolDataSourceDb
import world.respect.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import world.respect.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import world.respect.datalayer.school.model.Person
import world.respect.datalayer.school.model.PersonGenderEnum
import world.respect.datalayer.school.model.PersonRole
import world.respect.datalayer.school.model.PersonRoleEnum
import world.respect.datalayer.shared.XXHashUidNumberMapper
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.resources.XapiResource
import world.respect.libutil.ext.sanitizedForFilename
import world.respect.libxxhash.jvmimpl.XXStringHasherCommonJvm

class IpcTestApplication: Application(), XapiResourceProvider{

    internal val adminUserUid = "1"

    internal val json = Json { encodeDefaults = false }

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


    data class XapiResourceKey(
        val endpoint: Url,
        val auth: String,
    )

    private val resourceCache = Cache.Builder<XapiResourceKey, XapiResource>().build()

    override suspend fun provideXapiResource(
        endpoint: Url,
        authentication: String?
    ): XapiResource {
        return resourceCache.get(XapiResourceKey(endpoint, authentication ?: "")) {
            val schoolDb = Room.databaseBuilder<RespectSchoolDatabase>(
                this@IpcTestApplication, endpoint.sanitizedForFilename()
            ).build()

            SchoolDataSourceDb(
                schoolDb = schoolDb,
                uidNumberMapper = numMapper,
                authenticatedUser = authUser,
                checkPersonPermissionUseCase = CheckPersonPermissionUseCaseDbImpl(
                    authenticatedUser = authUser,
                    schoolDb = schoolDb,
                    uidNumberMapper = numMapper,
                ),
                json = json,
                defaultAppCatalogUrl = "http://localhost/not-used-here-buddy",
                schoolUrl = endpoint,
                authenticatedXapiAgentsUseCase = {
                    authentication?.let {
                        json.decodeFromString(ListSerializer(XapiAgent.serializer()), it)
                    } ?: emptyList()
                },
            ).also {
                it.personDataSource.updateLocal(listOf(adminPerson))

                AddDefaultSchoolPermissionGrantsUseCase(
                    schoolDb = schoolDb,
                    uidNumberMapper = numMapper,
                ).invoke()

            }.xapiResource
        }
    }

}