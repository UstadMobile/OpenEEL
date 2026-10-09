package world.respect.server.routes.passkey

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.lib.dataloadstate.throwable.withHttpStatus
import world.respect.shared.domain.account.passkey.GetActivePersonPasskeysUseCase

fun Route.GetAllActivePasskeysRoute(
    useCase: (ApplicationCall) -> GetActivePersonPasskeysUseCase,
) {
    get("allactivepasskeys") {

        val personGuid = call.request.queryParameters["personGuid"]
            ?: throw IllegalArgumentException("missing personGuid param").withHttpStatus(400)
        val response = useCase(call).getActivePeronPasskeys(
            personGuid = personGuid,
        )
        call.respond(response)
    }
}

