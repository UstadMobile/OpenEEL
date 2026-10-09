package world.respect.xapi.ipc.server.ext

import android.os.Bundle
import android.os.Message
import android.util.Log
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import world.respect.lib.dataloadstate.ext.toPrettyString
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.lib.xapi.resources.ISingleDocumentParams
import world.respect.lib.xapi.resources.XapiDocumentResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.server.XapiIpcResourceIncomingHandler
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcMethodEnum
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceAndMethod
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import world.respect.xapi.ipc.shared.messages.ext.getDataLoadParams
import world.respect.xapi.ipc.shared.messages.ext.sendResponseErrorMessage
import world.respect.xapi.ipc.shared.messages.ext.sendResponseMessage
import world.respect.xapi.ipc.shared.messages.ext.toBundle
import world.respect.xapi.ipc.shared.messages.ext.toXapiDocument
import java.util.concurrent.ExecutorService

abstract class AbstractDocumentResourceIncomingHandler<
        MultiDocParams: Any,
        SingleDocParams: ISingleDocumentParams<MultiDocParams>,
        T: XapiDocumentResource<MultiDocParams, SingleDocParams>
>(
    private val xapiResourceProvider: XapiResourceProvider,
    protected val json: Json,
    private val scope: CoroutineScope,
    private val executor: ExecutorService,
) : XapiIpcResourceIncomingHandler {

    abstract fun XapiResource.documentResource(): T

    abstract fun Bundle.getMultiDocParams(): MultiDocParams

    abstract fun Bundle.getSingleDocParams(): SingleDocParams

    private val flowCollectors = ConcurrentMap<Int, Job>()

    override fun handleMessage(msg: Message) {
        val requestBundle = msg.data
        val replyTo = msg.replyTo
        val incomingMessageId = msg.arg1

        if(flowCollectors.removeCollectorIfFlowCompleted(msg)) {
            return
        }

        val method = XapiIpcResourceAndMethod.fromArg2Int(msg.arg2).method

        val xapiResource = runBlocking {
            xapiResourceProvider.provideResource(requestBundle)
        }

        val docResource = xapiResource.documentResource()

        val logPrefix = "XapiIpcService (client=) msg #$incomingMessageId)"

        when(method) {
            XapiIpcMethodEnum.GET -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: get state")
                scope.launch {
                    replyTo.sendResponseMessage(
                        what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                        messageId = incomingMessageId,
                        data = docResource.get(
                            params = requestBundle.getSingleDocParams(),
                            dataLoadParams = requestBundle.getDataLoadParams(),
                        ).also {
                            Log.d(XapiIpcTags.LOGTAG, "$logPrefix get state: response ${it.toPrettyString()}")
                        }.toBundle(executor),
                    )
                }
            }

            XapiIpcMethodEnum.GET_AS_FLOW -> {
                scope.launch {
                    Log.d(XapiIpcTags.LOGTAG, "$logPrefix #$incomingMessageId getAsFlow state")

                    docResource.getAsFlow(
                        params = requestBundle.getSingleDocParams(),
                        dataLoadParams = requestBundle.getDataLoadParams(),
                    ).collect { dataLoadState ->
                        replyTo.sendResponseMessage(
                            what = IpcMessageBridgeWhatFlags.WHAT_FLOW_EMISSION,
                            messageId = incomingMessageId,
                            data = dataLoadState.toBundle(executor),
                        )

                        Log.d(
                            XapiIpcTags.LOGTAG,
                            "$logPrefix getAsFlow state emit ${dataLoadState.toPrettyString()}"
                        )
                    }
                }.also {
                    flowCollectors[incomingMessageId] = it
                }
            }

            XapiIpcMethodEnum.GET_MULTIDOC -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: getMultipleDocuments state")
                scope.launch {
                    replyTo.sendResponseMessage(
                        what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                        messageId = incomingMessageId,
                        data = docResource.getMultipleDocuments(
                            params = requestBundle.getMultiDocParams(),
                            dataLoadParams = requestBundle.getDataLoadParams(),
                        ).also {
                            Log.d(XapiIpcTags.LOGTAG, "$logPrefix getMultipleDocuments state: response ${it.toPrettyString()}")
                        }.toBundle(
                            ListSerializer(String.serializer()), json
                        ),
                    )
                }
            }

            XapiIpcMethodEnum.POST -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: post state")
                scope.launch {
                    try {
                        docResource.post(
                            params = requestBundle.getSingleDocParams(),
                            document = requestBundle.toXapiDocument(),
                        )

                        replyTo.sendResponseMessage(
                            what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                            messageId = incomingMessageId,
                            data = Bundle().apply {
                                putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                            }
                        )
                    } catch (e: Exception) {
                        replyTo.sendResponseErrorMessage(
                            messageId = incomingMessageId,
                            what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                            error = e,
                        )
                    }

                }
            }

            XapiIpcMethodEnum.PUT -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: put state")
                scope.launch {
                    try {
                        docResource.put(
                            params = requestBundle.getSingleDocParams(),
                            document = requestBundle.toXapiDocument(),
                        )

                        replyTo.sendResponseMessage(
                            what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                            messageId = incomingMessageId,
                            data = Bundle().apply {
                                putInt(XapiIpcKeys.KEY_STATUS_CODE, 200)
                            }
                        )
                    } catch (e: Exception) {
                        replyTo.sendResponseErrorMessage(
                            messageId = incomingMessageId,
                            what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                            error = e,
                        )
                    }
                }
            }

            XapiIpcMethodEnum.DELETE -> {
                Log.d(XapiIpcTags.LOGTAG, "$logPrefix: delete state")
                scope.launch {
                    try {
                        docResource.delete(
                            params = requestBundle.getSingleDocParams(),
                        )

                        replyTo.sendResponseMessage(
                            what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                            messageId = incomingMessageId,
                            data = Bundle().apply {
                                putInt(XapiIpcKeys.KEY_STATUS_CODE, 204)
                            }
                        )
                    } catch (e: Exception) {
                        replyTo.sendResponseErrorMessage(
                            messageId = incomingMessageId,
                            what = IpcMessageBridgeWhatFlags.WHAT_RESPONSE,
                            error = e,
                        )
                    }
                }
            }
        }
    }
}