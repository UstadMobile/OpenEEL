package org.openeel.demo.demolaunchableappserver

import io.ktor.http.ContentType
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import org.koin.ktor.ext.getKoin
import world.respect.server.demoapp.ext.demoAppBaseUrl
import world.respect.server.domain.school.demoapp.MakeDemoAppManifestUseCase

class DemoRouteClass()

fun Route.DemoLaunchableAppManifestRoute() {
    get("{lang}/${MakeDemoAppManifestUseCase.APP_MANIFEST_FILENAME}") {
        val makeDemoAppUseCase: MakeDemoAppManifestUseCase = getKoin().get()
        call.respond(
            makeDemoAppUseCase(
                baseUrl = call.demoAppBaseUrl(),
                langCode = call.parameters["lang"]!!
            )
        )
    }


    get(MakeDemoAppManifestUseCase.APP_MANIFEST_ICON_NAME) {
        call.respondBytes(
            bytes = DemoRouteClass::class.java.getResourceAsStream("/demoapp/app_icon.png")!!.readBytes(),
            contentType = ContentType.Image.PNG,
        )
    }
}
