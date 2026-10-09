package world.respect.server.routes

import io.ktor.server.request.header
import io.ktor.server.request.receive
import io.ktor.server.request.receiveText
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import world.respect.credentials.passkey.RespectCredential
import world.respect.credentials.passkey.RespectPasswordCredential
import world.respect.datalayer.school.model.DeviceInfo
import world.respect.server.util.ext.getSchoolKoinScope
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase
import world.respect.shared.domain.account.gettokenanduser.GetTokenAndUserProfileWithCredentialUseCase.Companion.PARAM_NAME_USERNAME

/**
 * Routes that handle issuing tokens.
 */
fun Route.AuthRoute() {

    post("auth-with-password") {
        val usernameParam = call.request.queryParameters[PARAM_NAME_USERNAME]
        val schoolScope = call.getSchoolKoinScope()
        val getTokenUseCase: GetTokenAndUserProfileWithCredentialUseCase = schoolScope.get()

        val credential: RespectCredential = if(usernameParam != null) {
            val password = call.receiveText().trim()
            RespectPasswordCredential(usernameParam, password)
        }else {
            call.receive()
        }
        val deviceInfo = call.request.header(DeviceInfo.HEADER_NAME)

        val authResponse = getTokenUseCase(
            credential = credential,
            deviceInfo = deviceInfo?.let { DeviceInfo.fromHeaderLineOrNull(it) },
        )

        call.respond(authResponse)
    }

}