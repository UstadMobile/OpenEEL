package world.respect.xapi.ipc.server

import android.content.Intent
import android.os.IBinder
import android.os.Messenger
import androidx.test.core.app.ApplicationProvider
import androidx.test.rule.ServiceTestRule
import io.ktor.http.Url
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.openeel.lib.ipc.messagebridge.IpcMessageBridgeMessengerImpl
import world.respect.lib.xapi.auth.GetAuthenticatedXapiAgentsUseCase
import world.respect.lib.xapi.model.XapiAgent
import world.respect.lib.xapi.resources.XapiResource
import world.respect.xapi.ipc.client.XapiResourceIpcClient
import world.respect.xapi.ipc.shared.messages.XapiIpcIntent
import java.util.concurrent.atomic.AtomicInteger
import kotlin.random.Random
import kotlin.test.assertNotNull

/**
 * Executes a test block with a bound [XapiResource] IPC client and clean database state.
 */

private val portAtomicInt = AtomicInteger(8000 + Random.nextInt(50_000))

suspend fun withXapiResourceIpcTest(
    serviceRule: ServiceTestRule,
    json: Json,
    authenticatedAgents: GetAuthenticatedXapiAgentsUseCase,
    block: suspend (XapiResource) -> Unit
) {
    val ipcTestApplication = ApplicationProvider.getApplicationContext<IpcTestApplication>()

    val intent = Intent(XapiIpcIntent.ACTION_XAPI_OVER_IPC)
    intent.`package` = ipcTestApplication.packageName

    val binder: IBinder? = serviceRule.bindService(intent)
    assertNotNull(binder)
    val serviceMessenger = Messenger(binder)

    val client = XapiResourceIpcClient(
        requestSender = IpcMessageBridgeMessengerImpl(serviceMessenger),
        json = json,
        endpoint = Url("http://localhost:${portAtomicInt.getAndIncrement()}/"),
        auth = json.encodeToString(
            ListSerializer(XapiAgent.serializer()), authenticatedAgents()
        ),
        clientPackageName = ipcTestApplication.packageName
    )

    try {
        block(client)
    } finally {
        client.close()
    }
}