package org.openeel.server.routes

import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import org.openeel.datalayer.SchoolDirectoryDataSource
import org.openeel.lib.dataloadstate.ktorserver.respondDataLoadState
import org.openeel.server.util.ext.virtualHost

/**
 * Serve the RespectRealm as a JSON according to the virtual host
 *
 * @param path typically "respect-school.json"
 */
fun Route.getRespectSchoolJson(
    path: String
) {
    val appDataSource: SchoolDirectoryDataSource = getKoin().get()

    get(path) {
        call.respondDataLoadState(
            appDataSource.schoolDirectoryEntryResource.getSchoolDirectoryEntryByUrl(
                url = call.virtualHost
            )
        )
    }
}