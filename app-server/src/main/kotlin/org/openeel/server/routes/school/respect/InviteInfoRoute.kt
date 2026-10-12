package org.openeel.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.invite.GetInviteInfoUseCase

fun Route.InviteInfoRoute(
    getInviteInfoUseCase: (ApplicationCall) -> GetInviteInfoUseCase,
) {
    get("info") {
        val code = call.request.queryParameters["code"]
            ?: throw IllegalArgumentException("missing code param").withHttpStatus(400)

        call.respond(
            getInviteInfoUseCase(call).invoke(
                code
            )
        )
    }
}