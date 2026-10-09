package world.respect.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.account.invite.RedeemInviteUseCase
import world.respect.shared.domain.account.invite.RespectRedeemInviteRequest

fun Route.RedeemInviteRoute(
    redeemInviteUseCase: (ApplicationCall) -> RedeemInviteUseCase
) {

    post("redeem") {
        val redeemRequest: RespectRedeemInviteRequest = call.receive()

        val isAuthenticated = call.principal<UserIdPrincipal>() != null

        val response = redeemInviteUseCase(call).invoke(
            redeemRequest,
            useActiveUserAuth = isAuthenticated
        )
        call.respond(response)
    }

}