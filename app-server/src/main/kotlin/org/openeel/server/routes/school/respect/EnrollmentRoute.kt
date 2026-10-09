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
import world.respect.datalayer.school.EnrollmentDataSource
import world.respect.datalayer.school.domain.GetPermissionLastModifiedUseCase
import world.respect.server.util.ext.offsetLimitPagingLoadParams
import world.respect.server.util.ext.requireAccountScope
import world.respect.server.util.ext.respondOffsetLimitPaging

fun Route.EnrollmentRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
    getPermissionLastModifiedUseCase: (ApplicationCall) -> GetPermissionLastModifiedUseCase = { call ->
        call.requireAccountScope().get()
    }
) {
    get(EnrollmentDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).enrollmentDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                listParams = EnrollmentDataSource.GetListParams.fromParams(call.request.queryParameters)
            ).invoke(),
            getPermissionLastModifiedUseCase = getPermissionLastModifiedUseCase(call),
        )
    }

    post(EnrollmentDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).enrollmentDataSource.store(
            list = call.receive()
        )
        call.respond(HttpStatusCode.NoContent)
    }
}