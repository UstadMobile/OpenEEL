package world.respect.xapi.ipc.shared.messages.ext

import io.ktor.http.Headers

fun Headers?.orEmpty() : Headers {
    return this ?: Headers.Empty
}
