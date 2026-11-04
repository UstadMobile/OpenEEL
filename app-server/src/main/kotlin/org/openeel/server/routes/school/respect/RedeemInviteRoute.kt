package org.openeel.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.openeel.shared.domain.account.invite.RespectRedeemInviteRequest
import org.openeel.shared.domain.account.invite.RedeemInviteUseCase

fun Route.RedeemInviteRoute(
    redeemInviteUseCase: (ApplicationCall) -> RedeemInviteUseCase
) {

    post("redeem") {
        val redeemRequest: RespectRedeemInviteRequest = call.receive()
        call.respond(redeemInviteUseCase(call).invoke(redeemRequest))
    }

}