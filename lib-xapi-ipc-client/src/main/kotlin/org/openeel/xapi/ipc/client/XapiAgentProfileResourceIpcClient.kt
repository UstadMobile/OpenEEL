package world.respect.xapi.ipc.client

import android.os.Bundle
import android.util.Log
import io.ktor.http.Url
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridge
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import org.openeel.lib.ipc.messagebridge.MessageData
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.dataloadstate.DataLoadParams
import world.respect.lib.dataloadstate.DataLoadState
import world.respect.lib.dataloadstate.ext.toPrettyString
import world.respect.lib.xapi.model.XapiDocument
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.xapi.ipc.client.ext.throwXapiExceptionIfStatusNotSuccessful
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import world.respect.xapi.ipc.shared.messages.ext.putAllFromStringMap
import world.respect.xapi.ipc.shared.messages.ext.putXapiIpcHeaders
import world.respect.xapi.ipc.shared.messages.ext.putXapiIpcQueryParameters
import world.respect.xapi.ipc.shared.messages.ext.toBundle
import world.respect.xapi.ipc.shared.messages.ext.toXapiDocumentDataLoadState
import java.util.concurrent.ExecutorService

class XapiAgentProfileResourceIpcClient(
    private val requestSender: IpcMessageBridge,
    private val json: Json,
    private val endpoint: Url,
    private val auth: String,
    private val messageDataExtras: Map<String, String>,
    private val executor: ExecutorService,
) : XapiAgentProfileResource {

    private fun Bundle.putEndpointAndExtras() {
        putString(XapiIpcKeys.KEY_ENDPOINT, endpoint.toString())
        putString(XapiIpcKeys.KEY_AUTH, auth)
        putAllFromStringMap(messageDataExtras)
    }

    override suspend fun getMultipleDocuments(
        params: XapiAgentProfileResource.MultiDocParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<List<String>> {
        Log.d(
            XapiIpcTags.LOGTAG,
            "XapiAgentProfileResourceIpcClient: getMultipleDocuments"
        )
        return requestSender.executeRequestAsDataLoadState(
            request = MessageData(
                data = Bundle().apply {
                    putEndpointAndExtras()
                    putXapiIpcQueryParameters(params.toParameters(json))
                    putXapiIpcHeaders(dataLoadParams.requestHeaders)
                },
                what = IpcMessageBridgeWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.AGENT_PROFILE_GET_MULTIDOC,
            ),
            json = json,
            deserializer = ListSerializer(String.serializer()),
        ).also {
            Log.d(
                XapiIpcTags.LOGTAG,
                "XapiAgentProfileResourceIpcClient: getMultipleDocuments response ${it.toPrettyString()}"
            )
        }
    }

    override suspend fun get(
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams,
    ): DataLoadState<XapiDocument> {
        Log.d(
            XapiIpcTags.LOGTAG,
            "XapiAgentProfileResourceIpcClient: get"
        )
        return try {
            val response = requestSender.executeForResponse(
                MessageData(
                    data = Bundle().apply {
                        putEndpointAndExtras()
                        putXapiIpcQueryParameters(params.toParameters(json))
                        putXapiIpcHeaders(dataLoadParams.requestHeaders)
                    },
                    what = IpcMessageBridgeWhatFlags.WHAT_REQUEST,
                    arg2 = XapiIpcResourceFlags.AGENT_PROFILE_GET,
                )
            )
            response.data.toXapiDocumentDataLoadState().also {
                Log.d(
                    XapiIpcTags.LOGTAG,
                    "XapiAgentProfileResourceIpcClient: get response ${it.toPrettyString()}"
                )
            }
        } catch (e: Throwable) {
            DataErrorResult(e)
        }
    }

    override fun getAsFlow(
        params: XapiAgentProfileResource.SingleDocumentParams,
        dataLoadParams: DataLoadParams,
    ): Flow<DataLoadState<XapiDocument>> {
        Log.d(
            XapiIpcTags.LOGTAG,
            "XapiAgentProfileResourceIpcClient: getAsFlow"
        )
        return requestSender.executeForFlow(
            messageData = MessageData(
                data = Bundle().apply {
                    putEndpointAndExtras()
                    putXapiIpcQueryParameters(params.toParameters(json))
                    putXapiIpcHeaders(dataLoadParams.requestHeaders)
                },
                what = IpcMessageBridgeWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.AGENT_PROFILE_GET_FLOW,
            )
        ).map { msg ->
            msg.data.toXapiDocumentDataLoadState().also {
                Log.d(
                    XapiIpcTags.LOGTAG,
                    "XapiAgentProfileResourceIpcClient: getAsFlow emit ${it.toPrettyString()}"
                )
            }
        }
    }

    override suspend fun post(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        Log.d(
            XapiIpcTags.LOGTAG,
            "XapiAgentProfileResourceIpcClient: post"
        )
        val docBundle = document.toBundle(executor)
        requestSender.executeForResponse(
            MessageData(
                data = Bundle().apply {
                    putEndpointAndExtras()
                    putXapiIpcQueryParameters(params.toParameters(json))
                    putAll(docBundle)
                },
                what = IpcMessageBridgeWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.AGENT_PROFILE_POST,
            )
        ).throwXapiExceptionIfStatusNotSuccessful()
    }

    override suspend fun put(
        params: XapiAgentProfileResource.SingleDocumentParams,
        document: XapiDocument,
    ) {
        Log.d(
            XapiIpcTags.LOGTAG,
            "XapiAgentProfileResourceIpcClient: put"
        )
        val docBundle = document.toBundle(executor)
        requestSender.executeForResponse(
            MessageData(
                data = Bundle().apply {
                    putEndpointAndExtras()
                    putXapiIpcQueryParameters(params.toParameters(json))
                    putAll(docBundle)
                },
                what = IpcMessageBridgeWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.AGENT_PROFILE_PUT,
            )
        ).throwXapiExceptionIfStatusNotSuccessful()
    }

    override suspend fun delete(
        params: XapiAgentProfileResource.SingleDocumentParams,
    ) {
        Log.d(
            XapiIpcTags.LOGTAG,
            "XapiAgentProfileResourceIpcClient: delete"
        )
        requestSender.executeForResponse(
            MessageData(
                data = Bundle().apply {
                    putEndpointAndExtras()
                    putXapiIpcQueryParameters(params.toParameters(json))
                },
                what = IpcMessageBridgeWhatFlags.WHAT_REQUEST,
                arg2 = XapiIpcResourceFlags.AGENT_PROFILE_DELETE,
            )
        ).throwXapiExceptionIfStatusNotSuccessful()
    }
}
