package org.openeel.server.demoapp.ext

import io.ktor.http.Url
import io.ktor.server.routing.RoutingCall
import org.openeel.libutil.ext.resolve
import org.openeel.server.util.ext.virtualHost

fun RoutingCall.demoAppBaseUrl(): Url {
    return virtualHost.resolve("/demoapp/")
}
