package org.openeel.xapi.ipc.server

import android.app.Application
import androidx.room.Room
import io.github.reactivecircus.cache4k.Cache
import io.ktor.http.Url
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.openeel.datalayer.AuthenticatedUserPrincipalId
import org.openeel.datalayer.db.RespectSchoolDatabase
import org.openeel.datalayer.db.SchoolDataSourceDb
import org.openeel.datalayer.db.school.domain.AddDefaultSchoolPermissionGrantsUseCase
import org.openeel.datalayer.db.school.domain.CheckPersonPermissionUseCaseDbImpl
import org.openeel.datalayer.school.model.Person
import org.openeel.datalayer.school.model.PersonGenderEnum
import org.openeel.datalayer.school.model.PersonRole
import org.openeel.datalayer.school.model.PersonRoleEnum
import org.openeel.datalayer.shared.XXHashUidNumberMapper
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.model.XapiAgent
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.libutil.ext.sanitizedForFilename
import org.openeel.libxxhash.jvmimpl.XXStringHasherCommonJvm

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