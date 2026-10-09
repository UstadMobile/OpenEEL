package org.openeel.server.routes.username

import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.openeel.lib.dataloadstate.throwable.withHttpStatus
import org.openeel.shared.domain.account.username.UsernameSuggestionUseCase

fun Route.UsernameSuggestionRoute(
    usernameSuggestionUseCase: (ApplicationCall) -> UsernameSuggestionUseCase
) {
    post("getsuggestion") {
        val name = call.request.queryParameters["name"]
            ?: throw IllegalStateException("No username found").withHttpStatus(400)

        val response = usernameSuggestionUseCase(call).invoke(
            name = name
        )
        call.respond(response)

    }
}