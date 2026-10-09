package world.respect.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.datalayer.SchoolDataSource
import world.respect.datalayer.school.InviteDataSource
import world.respect.datalayer.school.model.Invite2
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.InviteRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(InviteDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).inviteDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = InviteDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            ).invoke()
        )
    }

    post(InviteDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        val invites: List<Invite2> = call.receive()
        schoolDataSource.inviteDataSource.store(invites)
        call.respond(HttpStatusCode.NoContent)
    }
}
