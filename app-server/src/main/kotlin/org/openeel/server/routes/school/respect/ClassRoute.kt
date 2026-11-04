package org.openeel.server.routes.school.respect

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.header
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.ClassDataSource
import org.openeel.server.util.ext.offsetLimitPagingLoadParams
import org.openeel.server.util.ext.requireAccountScope
import org.openeel.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.ClassRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(ClassDataSource.ENDPOINT_NAME) {
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        call.respondOffsetLimitPaging(
            params = call.request.queryParameters.offsetLimitPagingLoadParams(),
            pagingSource = schoolDataSource(call).classDataSource.listAsPagingSource(
                loadParams = DataLoadParams(),
                params = ClassDataSource.GetListParams.fromParams(
                    call.request.queryParameters
                )
            ).invoke()
        )
    }

    post(ClassDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).classDataSource.store(
            list = call.receive()
        )
        call.respond(HttpStatusCode.NoContent)
    }

}