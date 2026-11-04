package org.openeel.xapi.ipc.server.ext

import android.os.Bundle
import io.ktor.http.Url
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.xapi.ipc.shared.messages.XapiIpcKeys

suspend fun XapiResourceProvider.provideResource(
    bundle: Bundle,
    endpoint: Url = bundle.getString(XapiIpcKeys.KEY_ENDPOINT)?.let { Url(it) }
        ?: throw IllegalArgumentException()
) : XapiResource {
    return provideXapiResource(
        endpoint = endpoint,
        authentication = bundle.getString(XapiIpcKeys.KEY_AUTH)
            ?: throw IllegalArgumentException("Bundle has no auth")
    )
}
