package org.openeel.xapi.ipc.client

import io.ktor.http.Url
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridge
import org.openeel.lib.xapi.resources.XapiActivitiesResource
import org.openeel.lib.xapi.resources.XapiActivityProfileResource
import org.openeel.lib.xapi.resources.XapiAgentProfileResource
import org.openeel.lib.xapi.resources.XapiAgentsResource
import org.openeel.lib.xapi.resources.XapiResource
import org.openeel.lib.xapi.resources.XapiStateResource
import org.openeel.lib.xapi.resources.XapiStatementsResource
import org.openeel.xapi.ipc.shared.messages.XapiIpcKeys

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

    override val activityProfile: XapiActivityProfileResource
        get() = TODO("Not yet implemented")

    override val agentProfile: XapiAgentProfileResource
        get() = TODO("Not yet implemented")

    override val state: XapiStateResource
        get() = TODO("Not yet implemented")

    override fun close() {
        requestSender.close()
    }

}