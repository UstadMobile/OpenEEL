package world.respect.server.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.util.reflect.typeInfo
import org.koin.ktor.ext.inject
import world.respect.datalayer.RespectAppDataSource
import world.respect.datalayer.respect.model.SchoolDirectoryEntry
import world.respect.datalayer.schooldirectory.SchoolDirectoryEntryDataSource
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.server.domain.school.add.AddSchoolUseCase
import world.respect.server.domain.school.add.InvalidSchoolRegistrationRequestException
import world.respect.server.domain.school.add.SchoolRegistrationDisabledException
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState
import world.respect.server.util.ext.virtualHost

const val AUTH_CONFIG_DIRECTORY_ADMIN_BASIC = "auth-directory-admin-basic"

/**
 * @param filterByHost if true, then filter the school directory entries to entries where the
 *        SchoolDirectoryEntry.inDirectoryUrl matches the virtual host for the request.
 */
fun Route.RespectSchoolDirectoryRoute(
    respectAppDataSource: RespectAppDataSource,
    filterByHost: Boolean = false,
) {
    get("school") {
        call.respondDataLoadState(
            dataLoadState = respectAppDataSource.schoolDirectoryEntryDataSource.list(
                loadParams = DataLoadParams(),
                listParams = SchoolDirectoryEntryDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                ).copy(
                    directoryUrl = if(filterByHost) {
                        call.request.virtualHost
                    }else {
                        null
                    }
                )
            ),
            typeInfo = typeInfo<List<SchoolDirectoryEntry>>()
        )
    }

    authenticate(AUTH_CONFIG_DIRECTORY_ADMIN_BASIC) {
        post("school") {
            val addSchoolUseCase: AddSchoolUseCase by inject()

            try {
                val addSchoolRequests: List<AddSchoolUseCase.AddSchoolRequest> = call.receive()
                addSchoolUseCase(addSchoolRequests)
                call.respond(HttpStatusCode.NoContent)
            } catch (e: SchoolRegistrationDisabledException) {
                call.respond(HttpStatusCode.Forbidden, "School registration is disabled")
            } catch (e: InvalidSchoolRegistrationRequestException) {
                call.respond(HttpStatusCode.BadRequest, e.message ?: "Invalid school domain")
            }
        }
    }
}