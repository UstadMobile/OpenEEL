package world.respect.xapi.ipc.server

import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStateResource
import world.respect.xapi.ipc.server.ext.AbstractDocumentResourceIncomingHandler
import world.respect.xapi.ipc.shared.messages.ext.getXapiIpcQueryParameters
import world.respect.xapi.ipc.shared.messages.ext.orEmpty
import java.util.concurrent.ExecutorService

class XapiIpcStateResourceIncomingHandler(
    xapiResourceProvider: XapiResourceProvider,
    json: Json,
    scope: CoroutineScope,
    executor: ExecutorService,
) : AbstractDocumentResourceIncomingHandler<
        XapiStateResource.MultiDocParams,
        XapiStateResource.SingleDocumentParams,
        XapiStateResource
>(
    xapiResourceProvider = xapiResourceProvider,
    json = json,
    scope = scope,
    executor = executor,
){
    override fun XapiResource.documentResource(): XapiStateResource {
        return state
    }

    override fun Bundle.getMultiDocParams(): XapiStateResource.MultiDocParams {
        return XapiStateResource.MultiDocParams.fromParameters(
            params = getXapiIpcQueryParameters().orEmpty(),
            json = json,
        )
    }

    override fun Bundle.getSingleDocParams(): XapiStateResource.SingleDocumentParams {
        return XapiStateResource.SingleDocumentParams.fromParameters(
            params = getXapiIpcQueryParameters().orEmpty(),
            json = json,
        )
    }
}