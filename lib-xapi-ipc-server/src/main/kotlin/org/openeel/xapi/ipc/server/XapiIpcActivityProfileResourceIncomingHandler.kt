package org.openeel.xapi.ipc.server

import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.xapi.ipc.server.ext.AbstractDocumentResourceIncomingHandler
import org.openeel.xapi.ipc.shared.messages.ext.getXapiIpcQueryParameters
import org.openeel.xapi.ipc.shared.messages.ext.orEmpty
import java.util.concurrent.ExecutorService

class XapiIpcActivityProfileResourceIncomingHandler(
    xapiResourceProvider: XapiResourceProvider,
    json: Json,
    scope: CoroutineScope,
    executor: ExecutorService,
) : AbstractDocumentResourceIncomingHandler<
        XapiActivityProfileResource.MultiDocParams,
        XapiActivityProfileResource.SingleDocumentParams,
        XapiActivityProfileResource
>(
    xapiResourceProvider = xapiResourceProvider,
    json = json,
    scope = scope,
    executor = executor,
) {
    override fun XapiResource.documentResource(): XapiActivityProfileResource {
        return activityProfile
    }

    override fun Bundle.getMultiDocParams(): XapiActivityProfileResource.MultiDocParams {
        return XapiActivityProfileResource.MultiDocParams.fromParameters(
            params = getXapiIpcQueryParameters().orEmpty(),
        )
    }

    override fun Bundle.getSingleDocParams(): XapiActivityProfileResource.SingleDocumentParams {
        return XapiActivityProfileResource.SingleDocumentParams.fromParameters(
            params = getXapiIpcQueryParameters().orEmpty(),
        )
    }
}
