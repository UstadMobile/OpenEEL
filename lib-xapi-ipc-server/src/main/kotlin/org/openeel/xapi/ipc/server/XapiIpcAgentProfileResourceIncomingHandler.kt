package org.openeel.xapi.ipc.server

import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.xapi.ipc.server.ext.AbstractDocumentResourceIncomingHandler
import org.openeel.xapi.ipc.shared.messages.ext.getXapiIpcQueryParameters
import org.openeel.xapi.ipc.shared.messages.ext.orEmpty
import java.util.concurrent.ExecutorService

class XapiIpcAgentProfileResourceIncomingHandler(
    xapiResourceProvider: XapiResourceProvider,
    json: Json,
    scope: CoroutineScope,
    executor: ExecutorService,
) : AbstractDocumentResourceIncomingHandler<
        XapiAgentProfileResource.MultiDocParams,
        XapiAgentProfileResource.SingleDocumentParams,
        XapiAgentProfileResource
>(
    xapiResourceProvider = xapiResourceProvider,
    json = json,
    scope = scope,
    executor = executor,
) {
    override fun XapiResource.documentResource(): XapiAgentProfileResource {
        return agentProfile
    }

    override fun Bundle.getMultiDocParams(): XapiAgentProfileResource.MultiDocParams {
        return XapiAgentProfileResource.MultiDocParams.fromParameters(
            params = getXapiIpcQueryParameters().orEmpty(),
            json = json,
        )
    }

    override fun Bundle.getSingleDocParams(): XapiAgentProfileResource.SingleDocumentParams {
        return XapiAgentProfileResource.SingleDocumentParams.fromParameters(
            params = getXapiIpcQueryParameters().orEmpty(),
            json = json,
        )
    }
}
