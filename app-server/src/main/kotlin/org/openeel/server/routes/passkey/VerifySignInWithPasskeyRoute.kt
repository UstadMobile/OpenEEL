package org.openeel.server.routes.passkey

import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.openeel.credentials.passkey.model.AuthenticationResponseJSON
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.passkey.VerifySignInWithPasskeyUseCase

fun Route.VerifySignInWithPasskeyRoute(
    useCase: (ApplicationCall) -> VerifySignInWithPasskeyUseCase,
) {
    post("verifypasskey") {

        val authenticationResponseJSON: AuthenticationResponseJSON = call.receive()
        val rpId = call.request.queryParameters["rpId"]
            ?: throw IllegalArgumentException("missing rpId param").withHttpStatus(400)
        val response = useCase(call).invoke(
            authenticationResponseJSON = authenticationResponseJSON,
            rpId = rpId,
        )
        call.respond(response)


    }
}

