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
import org.openeel.datalayer.school.PersonDataSource
import org.openeel.datalayer.school.model.Person
import org.openeel.server.util.ext.offsetLimitPagingLoadParams
import org.openeel.server.util.ext.requireAccountScope
import org.openeel.server.util.ext.respondOffsetLimitPaging

@Suppress("FunctionName")
fun Route.PersonRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(PersonDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        call.response.header(HttpHeaders.Vary, HttpHeaders.Authorization)
        val getListParams = PersonDataSource.GetListParams.fromParams(
            call.request.queryParameters
        )

        val loadParams = call.request.queryParameters.offsetLimitPagingLoadParams()

        call.respondOffsetLimitPaging(
            params = loadParams,
            pagingSource = schoolDataSource.personDataSource.listAsPagingSource(
                DataLoadParams(), getListParams
            ).invoke()
        )
    }

    post(PersonDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        val persons: List<Person> = call.receive()
        schoolDataSource.personDataSource.store(persons)
        call.respond(HttpStatusCode.NoContent)
    }

}