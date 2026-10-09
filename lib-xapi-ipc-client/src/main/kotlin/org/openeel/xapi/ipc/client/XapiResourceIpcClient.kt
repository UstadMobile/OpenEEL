package world.respect.xapi.ipc.client

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridge
import world.respect.lib.xapi.resources.XapiActivitiesResource
import world.respect.lib.xapi.resources.XapiActivityProfileResource
import world.respect.lib.xapi.resources.XapiAgentProfileResource
import world.respect.lib.xapi.resources.XapiAgentsResource
import world.respect.lib.xapi.resources.XapiResource
import world.respect.lib.xapi.resources.XapiStateResource
import world.respect.lib.xapi.resources.XapiStatementsResource
import world.respect.xapi.ipc.shared.messages.XapiIpcKeys
import java.util.concurrent.Executors

/**
 * XapiResourceIpcClient must host an interface implementation for messages for which a reply is
 * expected e.g.
 *
 * returns response
 * suspend fun sendRequest(request: Message): Message
 *  .. sends message using the messenger, waits for reply by using a deferred completable.
 *
 */
class XapiResourceIpcClient(
    private val requestSender: IpcMessageBridge,
    private val json: Json,
    private val endpoint: Url,
    private val auth: String,
    clientPackageName: String,
): XapiResource {

    private val messageExtras = mapOf(
        XapiIpcKeys.KEY_CLIENT_PACKAGE to clientPackageName,
    )

    private val executor = Executors.newCachedThreadPool()

    override val statements: XapiStatementsResource by lazy {
        XapiStatementsResourceIpcClient(
            requestSender = requestSender,
            json = json,
            endpoint = endpoint,
            auth = auth,
            messageDataExtras = messageExtras,
        )
    }

    override val agents: XapiAgentsResource
        get() = TODO("Not yet implemented")

    override val activities: XapiActivitiesResource
        get() = TODO("Not yet implemented")

    override val activityProfile: XapiActivityProfileResource by lazy {
        XapiActivityProfileResourceIpcClient(
            requestSender = requestSender,
            json = json,
            endpoint = endpoint,
            auth = auth,
            messageDataExtras = messageExtras,
            executor = executor,
        )
    }

    override val agentProfile: XapiAgentProfileResource by lazy {
        XapiAgentProfileResourceIpcClient(
            requestSender = requestSender,
            json = json,
            endpoint = endpoint,
            auth = auth,
            messageDataExtras = messageExtras,
            executor = executor,
        )
    }

    override val state: XapiStateResource by lazy {
        XapiStateResourceIpcClient(
            requestSender = requestSender,
            json = json,
            endpoint = endpoint,
            auth = auth,
            messageDataExtras = messageExtras,
            executor = executor,
        )
    }

    override fun close() {
        requestSender.close()
        executor.shutdown()
    }

}