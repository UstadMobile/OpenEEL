package world.respect.server.routes.school.respect

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.shared.domain.account.child.AddChildAccountUseCase

fun Route.AddChildAccountRoute(
    addChildAccountUseCase: (ApplicationCall) -> AddChildAccountUseCase
) {
    post(AddChildAccountUseCase.ENDPOINT_NAME) {
        val addChildAccountRequest: AddChildAccountUseCase.AddChildAccountRequest = call.receive()

        call.respond(addChildAccountUseCase(call).invoke(addChildAccountRequest))
    }

}