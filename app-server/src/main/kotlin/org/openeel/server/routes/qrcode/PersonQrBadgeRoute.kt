package world.respect.server.routes.qrcode

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonQrBadgeDataSource
import world.respect.server.util.ext.requireAccountScope
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState

fun Route.PersonQrBadgeRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(PersonQrBadgeDataSource.ENDPOINT_NAME) {
        call.respondDataLoadState(
            schoolDataSource(call).personQrBadgeDataSource.listAll(
                loadParams = DataLoadParams(),
                listParams = PersonQrBadgeDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }

    post(PersonQrBadgeDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).personQrBadgeDataSource.store(call.receive())
        call.respond(HttpStatusCode.NoContent)
    }
}