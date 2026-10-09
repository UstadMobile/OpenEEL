package org.openeel.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.SchoolPermissionGrantDataSource
import org.openeel.server.util.ext.requireAccountScope
import org.openeel.lib.dataloadstate.ktorserver.respondDataLoadState

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