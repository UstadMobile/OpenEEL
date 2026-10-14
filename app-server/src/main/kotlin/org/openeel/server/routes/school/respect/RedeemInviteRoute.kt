package org.openeel.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.openeel.shared.domain.account.invite.RedeemInviteRequest
import org.openeel.shared.domain.account.invite.RedeemInviteUseCase

fun Route.RedeemInviteRoute(
    redeemInviteUseCase: (ApplicationCall) -> RedeemInviteUseCase
) {

    post("redeem") {
     val redeemRequest: RedeemInviteRequest = call.receive()

        val isAuthenticated = call.principal<UserIdPrincipal>() != null

        val response = redeemInviteUseCase(call).invoke(
            redeemRequest,
            useActiveUserAuth = isAuthenticated
        )
        call.respond(response)
    }

}