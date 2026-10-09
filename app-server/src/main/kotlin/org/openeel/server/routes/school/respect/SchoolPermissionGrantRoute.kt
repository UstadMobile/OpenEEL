package world.respect.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.SchoolPermissionGrantDataSource
import world.respect.server.util.ext.requireAccountScope
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState

fun Route.SchoolPermissionGrantRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(SchoolPermissionGrantDataSource.ENDPOINT_NAME){
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondDataLoadState(
            schoolDataSource(call).schoolPermissionGrantDataSource.list(
                loadParams = DataLoadParams(),
                params = SchoolPermissionGrantDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }
}