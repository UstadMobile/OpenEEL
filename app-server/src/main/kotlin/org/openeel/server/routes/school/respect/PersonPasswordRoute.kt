package world.respect.server.routes.school.respect

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.PersonPasswordDataSource
import world.respect.server.util.ext.requireAccountScope
import world.respect.lib.dataloadstate.ktorserver.respondDataLoadState

fun Route.PersonPasswordRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {

    get(PersonPasswordDataSource.ENDPOINT_NAME) {
        call.respondDataLoadState(
            schoolDataSource(call).personPasswordDataSource.listAll(
                PersonPasswordDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            )
        )
    }

    post(PersonPasswordDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).personPasswordDataSource.store(call.receive())
        call.respond(HttpStatusCode.NoContent)
    }

}