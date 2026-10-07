package org.openeel.server.routes.qrcode

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.PersonQrBadgeDataSource
import org.openeel.server.util.ext.requireAccountScope
import org.openeel.lib.dataloadstate.ktorserver.respondDataLoadState

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