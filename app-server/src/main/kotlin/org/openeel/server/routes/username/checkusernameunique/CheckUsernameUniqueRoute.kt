package world.respect.server.routes.username.checkusernameunique

import io.ktor.http.CacheControl
import io.ktor.server.application.ApplicationCall
import io.ktor.server.response.cacheControl
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import world.respect.lib.dataloadstate.throwable.withHttpStatus
import world.respect.shared.domain.account.username.checkusernameunique.CheckUsernameUniqueUseCase

fun Route.CheckUsernameUniqueRoute(
    checkUsernameUniqueUseCase: (ApplicationCall) -> CheckUsernameUniqueUseCase
) {
    get(CheckUsernameUniqueUseCase.ENDPOINT_NAME) {
        val username = call.request.queryParameters[CheckUsernameUniqueUseCase.PARAM_USERNAME]
            ?: throw IllegalArgumentException("username required").withHttpStatus(400)
        call.response.cacheControl(CacheControl.NoStore(null))
        call.respond(checkUsernameUniqueUseCase(call).invoke(username))
    }
}