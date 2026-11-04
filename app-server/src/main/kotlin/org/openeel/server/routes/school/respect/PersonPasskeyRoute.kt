package org.openeel.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.openeel.datalayer.SchoolDataSource
import org.openeel.datalayer.school.PersonPasskeyDataSource
import org.openeel.server.util.ext.requireAccountScope
import org.openeel.lib.dataloadstate.ktorserver.respondDataLoadState

fun Route.PersonPasskeyRoute(
    schoolDataSource: (ApplicationCall) -> SchoolDataSource = { call ->
        call.requireAccountScope().get()
    },
) {
    get(PersonPasskeyDataSource.ENDPOINT_NAME) {
        val schoolDataSource = schoolDataSource(call)
        call.respondDataLoadState(
            schoolDataSource.personPasskeyDataSource.listAll()
        )
    }

    post(PersonPasskeyDataSource.ENDPOINT_NAME) {
        schoolDataSource(call).personPasskeyDataSource.store(call.receive())
    }

}