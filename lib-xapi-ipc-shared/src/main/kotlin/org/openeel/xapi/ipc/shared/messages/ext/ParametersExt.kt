package world.respect.xapi.ipc.shared.messages.ext

import io.ktor.http.Parameters

/**
 *
 */
fun Parameters?.orEmpty() : Parameters {
    return this ?: Parameters.Empty
}
