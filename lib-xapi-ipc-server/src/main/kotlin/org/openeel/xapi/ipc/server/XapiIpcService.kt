package world.respect.xapi.ipc.server

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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import world.respect.lib.dataloadstate.DataErrorResult
import world.respect.lib.xapi.XapiResourceProvider
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import world.respect.xapi.ipc.shared.messages.XapiIpcTags
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeWhatFlags
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceAndMethod
import world.respect.xapi.ipc.shared.messages.XapiIpcResourceEnum
import world.respect.xapi.ipc.shared.messages.ext.toBundle
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.getValue

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

    private val executor = Executors.newCachedThreadPool()

    internal class IncomingHandler(
        looper: Looper,
        private val context: Context,
        private val applicationContext: Context = context.applicationContext,
        private val json: Json,
        private val executor: ExecutorService,
    ):  Handler(looper) {

        private val scope = CoroutineScope(Dispatchers.Default + Job())

        private val statementsHandler by lazy {
            StatementsResourceIncomingHandler(
                xapiResourceProvider = applicationContext as XapiResourceProvider,
                json = json,
                scope = scope,
            )
        }

        private val stateResourceHandler by lazy {
            XapiIpcStateResourceIncomingHandler(
                xapiResourceProvider = applicationContext as XapiResourceProvider,
                json = json,
                scope = scope,
                executor = executor,
            )
        }

        private val activityProfileResourceHandler by lazy {
            XapiIpcActivityProfileResourceIncomingHandler(
                xapiResourceProvider = applicationContext as XapiResourceProvider,
                json = json,
                scope = scope,
                executor = executor,
            )
        }

        private val agentProfileResourceHandler by lazy {
            XapiIpcAgentProfileResourceIncomingHandler(
                xapiResourceProvider = applicationContext as XapiResourceProvider,
                json = json,
                scope = scope,
                executor = executor,
            )
        }

        override fun handleMessage(msg: Message) {
            if(msg.what != IpcMessageBridgeWhatFlags.WHAT_REQUEST && msg.what != IpcMessageBridgeWhatFlags.WHAT_FLOW_COMPLETION) {
                super.handleMessage(msg)
                return
            }

            val incomingMessageId = msg.arg1

            val callingPackage = msg.data.getString(XapiIpcKeys.KEY_CLIENT_PACKAGE)

            val logPrefix = "XapiIpcService (client=$callingPackage) msg #$incomingMessageId)"
            val replyTo = msg.replyTo

            try {

                val resourceAndOp = XapiIpcResourceAndMethod.fromArg2Int(msg.arg2)

                when(resourceAndOp.resource) {
                    XapiIpcResourceEnum.STATEMENTS -> {
                        statementsHandler.handleMessage(msg)
                    }

                    XapiIpcResourceEnum.STATE -> {
                        stateResourceHandler.handleMessage(msg)
                    }

                    XapiIpcResourceEnum.ACTIVITY_PROFILE -> {
                        activityProfileResourceHandler.handleMessage(msg)
                    }

                    XapiIpcResourceEnum.AGENT_PROFILE -> {
                        agentProfileResourceHandler.handleMessage(msg)
                    }
                }

            }catch(e: Throwable) {
                Log.e(XapiIpcTags.LOGTAG, "$logPrefix: Error handling message", e)
                val replyMessage = Message.obtain(
                    this@IncomingHandler, IpcMessageBridgeWhatFlags.WHAT_RESPONSE
                )

                //Mark it as a response to the request id received.
                replyMessage.arg1 = incomingMessageId
                replyMessage.data = DataErrorResult<String>(
                    error = e,
                ).toBundle(String.serializer(), json)
                replyTo.send(replyMessage)
            }
        }
    }

    private val messenger: Messenger by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        Messenger(
            IncomingHandler(
                looper = handlerThread.looper,
                context = this,
                json = json,
                executor = executor,
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
        executor.shutdown()
    }
}