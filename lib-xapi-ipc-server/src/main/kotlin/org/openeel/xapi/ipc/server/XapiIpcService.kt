package org.openeel.xapi.ipc.server

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.HandlerThread
import android.os.IBinder
import android.os.Looper
import android.os.Message
import android.os.Messenger
import android.util.Log
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.Url
import io.ktor.util.collections.ConcurrentMap
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import net.thauvin.erik.urlencoder.UrlEncoderUtil
import org.openeel.lib.dataloadstate.DataErrorResult
import org.openeel.lib.dataloadstate.DataLoadParams
import org.openeel.lib.dataloadstate.ext.toPrettyString
import org.openeel.lib.xapi.OpenEelXapiConstants
import org.openeel.lib.xapi.XapiResourceProvider
import org.openeel.lib.xapi.exceptions.XapiException
import org.openeel.lib.xapi.ext.asAssignmentRecipeStmtIfIdNotNull
import org.openeel.lib.xapi.model.XapiStatement
import org.openeel.lib.xapi.model.XapiStatementResult
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.libutil.ext.normalizeForEndpoint
import org.openeel.xapi.ipc.shared.messages.XapiIpcKeys
import org.openeel.xapi.ipc.shared.messages.XapiIpcResourceFlags
import org.openeel.xapi.ipc.shared.messages.XapiIpcTags
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import org.openeel.xapi.ipc.shared.messages.ext.getDeserialized
import org.openeel.xapi.ipc.shared.messages.ext.getStringValues
import org.openeel.xapi.ipc.shared.messages.ext.toBundle
import kotlin.uuid.Uuid

/**
 * Messenger service (server) that will receive xAPI requests, send them to a given xAPI resource,
 * and then send a reply message with the response.
 *
 * The replyTo field of the incoming message MUST be a Messenger of the client where responses
 * should be sent.
 *
 * See
 * https://developer.android.com/develop/background-work/services/bound-services#Messenger
 * https://android.googlesource.com/platform/development/+/master/samples/ApiDemos/src/com/example/android/apis/app/MessengerService.java
 *
 * Clients will register: then registered clients will receive invalidation messages, which can be
 * easily observed by the datasource on the other side.
 */
class XapiIpcService: Service() {

    private val json = Json {
        encodeDefaults = false
    }

    private val handlerThread = HandlerThread("XapiIpcServiceThread").also {
        if(!it.isAlive)
            it.start()
    }

    internal class IncomingHandler(
        looper: Looper,
        private val context: Context,
        private val applicationContext: Context = context.applicationContext,
        private val json: Json,
    ):  Handler(looper) {

        private val flowCollectors = ConcurrentMap<Int, Job>()

        private val scope = CoroutineScope(Dispatchers.Default + Job())

        override fun handleMessage(msg: Message) {
            if(msg.what != IpcMessageBridgeWhatFlags.WHAT_REQUEST && msg.what != IpcMessageBridgeWhatFlags.WHAT_FLOW_COMPLETION) {
                super.handleMessage(msg)
                return
            }

            val incomingMessageId = msg.arg1

            val callingPackage = msg.data.getString(XapiIpcKeys.KEY_CLIENT_PACKAGE)

            val logPrefix = "XapiIpcService (client=$callingPackage) msg #$incomingMessageId"

            if(msg.what == IpcMessageBridgeWhatFlags.WHAT_FLOW_COMPLETION) {
                flowCollectors.remove(incomingMessageId)?.also {
                    it.cancel()
                    Log.d(XapiIpcTags.LOGTAG, "$logPrefix Flow cancelled")
                }

                return
            }

            val replyMessage = Message.obtain(
                this@IncomingHandler, IpcMessageBridgeWhatFlags.WHAT_RESPONSE
            )

            //Mark it as a response to the request id received.
            replyMessage.arg1 = incomingMessageId

            val replyTo = msg.replyTo

            try {
                val xapiResourceProvider = applicationContext as? XapiResourceProvider
                    ?: throw IllegalStateException("No xapi resource provider")

                val endpoint = msg.data.getString(XapiIpcKeys.KEY_ENDPOINT)?.let {
                    Url(it)
                } ?: throw IllegalArgumentException("Message has no endpoint")

                val assignmentSegmentIndex = endpoint.segments.indexOf(
                    OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
                )

                val assignmentActivityId = if(assignmentSegmentIndex >= 0) {
                    UrlEncoderUtil.decode(endpoint.segments[assignmentSegmentIndex + 1])
                }else {
                    null
                }

                val scopeEndpoint = if(assignmentActivityId != null){
                    URLBuilder(endpoint).apply {
                        //Builder adds a blank segment at the beginning, so this needs done again
                        val segmentIndex = pathSegments.indexOf(
                            OpenEelXapiConstants.ASSIGNMENT_XAPI_SEGMENT
                        )

                        pathSegments = pathSegments.filterIndexed { index, _ ->
                            index != segmentIndex && index != (segmentIndex + 1)
                        }

                        normalizeForEndpoint()
                    }.build()
                }else {
                    endpoint
                }

                val auth = msg.data.getString(XapiIpcKeys.KEY_AUTH)
                    ?: throw IllegalArgumentException("Message has no auth")

                val xapiResource = runBlocking {
                    xapiResourceProvider.provideXapiResource(scopeEndpoint, auth)
                }

                when (msg.arg2) {
                    XapiIpcResourceFlags.POST_STATEMENTS -> {
                        replyMessage.data = runBlocking {
                            xapiResource.statements.post(
                                list = msg.data.getDeserialized(
                                    key = XapiIpcKeys.KEY_BODY,
                                    json = json,
                                    deserializer = ListSerializer(
                                        XapiStatement.serializer()
                                    ),
                                )?.also {
                                    Log.d(
                                        XapiIpcTags.LOGTAG,
                                        "$logPrefix post send ${it.size} statements"
                                    )
                                }?.map {
                                    it.asAssignmentRecipeStmtIfIdNotNull(assignmentActivityId)
                                } ?: throw XapiException(400, "Post statements has no body")
                            ).also {
                                Log.d(XapiIpcTags.LOGTAG, "$logPrefix post: response ${it.toPrettyString()}")
                            }.toBundle(ListSerializer(Uuid.serializer()), json)
                        }

                        msg.replyTo.send(replyMessage)
                    }

                    XapiIpcResourceFlags.GET_STATEMENTS -> {
                        Log.d(XapiIpcTags.LOGTAG, "$logPrefix: get ")
                        replyMessage.data = runBlocking {
                            xapiResource.statements.get(
                                listParams = XapiStatementsResource.GetStatementParams.fromParams(
                                    params = msg.data.getStringValues(
                                        XapiIpcKeys.KEY_QUERY_PARAMS
                                    ) ?: Parameters.Empty,
                                    json = json
                                )
                            ).also {
                                Log.d(XapiIpcTags.LOGTAG, "$logPrefix get: response ${it.toPrettyString()}")
                            }.toBundle(XapiStatementResult.serializer(), json)
                        }

                        msg.replyTo.send(replyMessage)
                    }

                    XapiIpcResourceFlags.GET_STATEMENTS_FLOW -> {
                        scope.launch {
                            Log.d(XapiIpcTags.LOGTAG, "$logPrefix #$incomingMessageId getAsFlow")
                            xapiResource.statements.getAsFlow(
                                listParams = XapiStatementsResource.GetStatementParams.fromParams(
                                    params = msg.data.getStringValues(
                                        XapiIpcKeys.KEY_QUERY_PARAMS
                                    ) ?: Parameters.Empty,
                                    json = json
                                ),
                                dataLoadParams = DataLoadParams()
                            ).collect {
                                val message = Message.obtain()
                                message.arg1 = incomingMessageId
                                message.what = IpcMessageBridgeWhatFlags.WHAT_FLOW_EMISSION
                                message.data = it.toBundle(XapiStatementResult.serializer(), json)
                                Log.d(
                                    XapiIpcTags.LOGTAG,
                                    "$logPrefix getAsFlow emit ${it.toPrettyString()}"
                                )
                                replyTo.send(message)
                            }
                        }.also {
                            flowCollectors[incomingMessageId] = it
                        }
                    }

                    else -> {
                        super.handleMessage(msg)
                    }
                }
            }catch(e: Throwable) {
                replyMessage.data = DataErrorResult<String>(
                    error = e,
                ).toBundle(String.serializer(), json)
                msg.replyTo.send(replyMessage)
            }
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(
            IncomingHandler(
                looper = handlerThread.looper,
                context = this,
                json = json,
            )
        )
    }

    override fun onBind(intent: Intent): IBinder? {
        val clientAppPkg = intent.getStringExtra(XapiIpcKeys.KEY_CLIENT_PACKAGE) ?: ""
        Log.d(XapiIpcTags.LOGTAG, "XapiIpcService: onBind: ${intent.action} client=$clientAppPkg")
        return messenger.binder
    }


    override fun onDestroy() {
        Log.d(XapiIpcTags.LOGTAG, "XapiIpcService: onDestroy")
        super.onDestroy()
        handlerThread.quit()
    }
}