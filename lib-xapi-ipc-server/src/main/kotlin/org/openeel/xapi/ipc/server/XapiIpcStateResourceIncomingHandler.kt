package org.openeel.xapi.ipc.server

import android.os.Bundle
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.xapi.ipc.server.ext.AbstractDocumentResourceIncomingHandler
import org.openeel.xapi.ipc.shared.messages.ext.getXapiIpcQueryParameters
import org.openeel.xapi.ipc.shared.messages.ext.orEmpty
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