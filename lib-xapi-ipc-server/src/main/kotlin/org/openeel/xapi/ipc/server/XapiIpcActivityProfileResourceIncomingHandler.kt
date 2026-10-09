package world.respect.xapi.ipc.server

import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.server.ext.AbstractDocumentResourceIncomingHandler
import world.respect.xapi.ipc.shared.messages.ext.getXapiIpcQueryParameters
import world.respect.xapi.ipc.shared.messages.ext.orEmpty
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
