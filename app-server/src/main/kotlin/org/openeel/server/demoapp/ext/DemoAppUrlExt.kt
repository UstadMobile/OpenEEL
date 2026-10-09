package world.respect.server.demoapp.ext

import io.ktor.http.Url
import io.ktor.server.routing.RoutingCall
import world.respect.libutil.ext.resolve
import world.respect.server.util.ext.virtualHost

fun RoutingCall.demoAppBaseUrl(): Url {
    return virtualHost.resolve("/demoapp/")
}
