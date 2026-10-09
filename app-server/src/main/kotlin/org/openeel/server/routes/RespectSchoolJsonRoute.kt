package world.respect.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import world.respect.datalayer.RespectAppDataSource
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState
import world.respect.server.util.ext.virtualHost

/**
 * Serve the RespectRealm as a JSON according to the virtual host
 *
 * @param path typically "respect-school.json"
 */
fun Route.getRespectSchoolJson(
    path: String
) {
    val appDataSource: RespectAppDataSource = getKoin().get()

    get(path) {
        call.respondDataLoadState(
            appDataSource.schoolDirectoryEntryDataSource.getSchoolDirectoryEntryByUrl(
                url = call.virtualHost
            )
        )
    }
}